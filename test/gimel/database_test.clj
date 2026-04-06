(ns gimel.database-test
  (:require [clojure.test :refer :all]
            [gimel.database :as db]))

(deftest match-valid-table-or-column-name?-test
  (testing "match-valid-table-or-column-name? function"
    (is (true? (#'db/match-valid-table-or-column-name? "valid_name")))
    (is (true? (#'db/match-valid-table-or-column-name? "anotherValidName123")))
    (is (true? (#'db/match-valid-table-or-column-name? "with spaces")))
    (is (thrown? clojure.lang.ExceptionInfo (#'db/match-valid-table-or-column-name? "invalid-name")))
    (is (thrown? clojure.lang.ExceptionInfo (#'db/match-valid-table-or-column-name? "invalid'name")))
    (is (thrown? clojure.lang.ExceptionInfo (#'db/match-valid-table-or-column-name? "invalid;name")))))
