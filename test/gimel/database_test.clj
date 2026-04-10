(ns gimel.database-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [next.jdbc :as jdbc]
            [gimel.database :as db]))

;; ---------------------------------------------------------------------------
;; In-memory SQLite datasource for test isolation.
;; SQLite :memory: databases are connection-scoped, so we open a single
;; persistent connection and wrap it as a datasource for the duration of
;; each test.
;; ---------------------------------------------------------------------------

(defn create-schema! [ds]
  (jdbc/execute! ds ["CREATE TABLE IF NOT EXISTS pages (
                        id INTEGER PRIMARY KEY,
                        url VARCHAR(128) NOT NULL,
                        title VARCHAR(128) NOT NULL,
                        content TEXT,
                        UNIQUE(url))"]))

(defn drop-schema! [ds]
  (jdbc/execute! ds ["DROP TABLE IF EXISTS pages"]))

(defn db-fixture [f]
  (let [raw-ds (jdbc/get-datasource {:dbtype "sqlite" :dbname ":memory:"})]
    (with-open [conn (jdbc/get-connection raw-ds)]
      (let [conn-ds (jdbc/with-options conn {})]
        (with-redefs [db/datasource conn-ds]
          (create-schema! conn-ds)
          (f)
          (drop-schema! conn-ds))))))

(use-fixtures :each db-fixture)

;; ---------------------------------------------------------------------------
;; Unit tests
;; ---------------------------------------------------------------------------

(deftest test-match-valid-table-or-column-name
  (testing "accepts alphanumeric and underscore names"
    (is (true? (#'db/match-valid-table-or-column-name? "pages")))
    (is (true? (#'db/match-valid-table-or-column-name? "my_table")))
    (is (true? (#'db/match-valid-table-or-column-name? "Column123"))))
  (testing "throws on SQL injection strings"
    (is (thrown? clojure.lang.ExceptionInfo
                 (#'db/match-valid-table-or-column-name? "; DROP TABLE pages")))
    (is (thrown? clojure.lang.ExceptionInfo
                 (#'db/match-valid-table-or-column-name? "table--comment")))
    (is (thrown? clojure.lang.ExceptionInfo
                 (#'db/match-valid-table-or-column-name? "foo'bar")))))

(deftest test-insert-page-idempotent
  (testing "insert-page returns the same id on a second call with the same URL"
    (let [id1 (db/insert-page "https://example.com/page" "Title" "Content")
          id2 (db/insert-page "https://example.com/page" "Title" "Content")]
      (is (integer? id1))
      (is (= id1 id2)))))

(deftest test-get-page-id
  (testing "returns the correct id after insert"
    (let [id (db/insert-page "https://example.com/test" "Test" "Body")]
      (is (= id (db/get-page-id "https://example.com/test")))))
  (testing "returns nil for unknown URLs"
    (is (nil? (db/get-page-id "https://example.com/does-not-exist")))))

(deftest test-insert-data
  (testing "inserts page and metadata rows without error"
    (is (nil? (db/insert-data
               "https://example.com/article"
               {:Title "My Article" :author "Alice" :tags ["clojure" "testing"]}
               "Some content")))))

;; ---------------------------------------------------------------------------
;; Property-based test — Property 3: insert-page is idempotent
;; Validates: Requirements 8.3
;; ---------------------------------------------------------------------------

(defspec prop-insert-page-idempotent 100
  (prop/for-all [[url title content] (gen/tuple gen/string-alphanumeric
                                                gen/string-alphanumeric
                                                gen/string)]
    (let [raw-ds (jdbc/get-datasource {:dbtype "sqlite" :dbname ":memory:"})]
      (with-open [conn (jdbc/get-connection raw-ds)]
        (let [conn-ds (jdbc/with-options conn {})]
          (with-redefs [db/datasource conn-ds]
            (create-schema! conn-ds)
            (let [id1 (db/insert-page url title content)
                  id2 (db/insert-page url title content)]
              (and (integer? id1)
                   (= id1 id2)))))))))
