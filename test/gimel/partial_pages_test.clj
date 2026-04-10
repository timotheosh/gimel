(ns gimel.partial-pages-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [next.jdbc :as jdbc]
            [gimel.database :as db]
            [gimel.partial-pages :refer [parse-header partial-pages]]))

;; ---------------------------------------------------------------------------
;; In-memory SQLite datasource for test isolation.
;; partial-pages calls db/insert-data internally, so we redirect the
;; datasource to an in-memory SQLite connection for each test.
;; ---------------------------------------------------------------------------

(defn create-schema! [ds]
  (jdbc/execute! ds ["CREATE TABLE IF NOT EXISTS pages (id INTEGER PRIMARY KEY, url VARCHAR(128) NOT NULL, title VARCHAR(128) NOT NULL, content TEXT, UNIQUE(url))"]))

(defn db-fixture [f]
  (let [raw-ds (jdbc/get-datasource {:dbtype "sqlite" :dbname ":memory:"})]
    (with-open [conn (jdbc/get-connection raw-ds)]
      (let [conn-ds (jdbc/with-options conn {})]
        (with-redefs [db/datasource conn-ds]
          (create-schema! conn-ds)
          (f))))))

(use-fixtures :each db-fixture)

;; ---------------------------------------------------------------------------
;; Unit tests
;; ---------------------------------------------------------------------------

(deftest test-parse-header-valid-yaml
  (testing "valid YAML string returns parsed map"
    (let [result (parse-header "title: My Page\nauthor: Alice")]
      (is (map? result))
      (is (= "My Page" (or (:title result) (get result "title")))))))

(deftest test-parse-header-invalid-yaml
  (testing "invalid YAML (tab indentation) returns map with :error NO META DATA"
    ;; A tab character in block context triggers a ScannerException in SnakeYAML
    (let [result (parse-header "key: value\n\tbad: indent")]
      (is (map? result))
      (is (= "NO META DATA" (:error result))))))

(deftest test-partial-pages-excludes-navbar
  (testing "partial-pages excludes /navbar.html from the returned map"
    (let [pages {"/navbar.html" "---\ntitle: Navbar\n---\n<nav></nav>"
                 "/index.html"  "---\ntitle: Home\n---\n<p>Hello</p>"}
          result (partial-pages pages)]
      (is (not (contains? result "/navbar.html")))
      (is (contains? result "/index.html")))))
