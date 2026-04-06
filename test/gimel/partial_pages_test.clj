(ns gimel.partial-pages-test
  (:require [clojure.test :refer :all]
            [gimel.partial-pages :as pp]))

(deftest parse-header-test
  (testing "parse-header function"
    (testing "with valid yaml"
      (let [yaml-string "title: My Page\nauthor: Me"
            result (pp/parse-header yaml-string)]
        (is (= {:title "My Page", :author "Me"} result))))
    (testing "with invalid yaml"
      (let [yaml-string "title: My Page\nauthor: Me:"
            result (pp/parse-header yaml-string)]
        (is (= {:title "NO META DATA", :error "NO META DATA"} result))))
    (testing "with empty string"
      (let [yaml-string ""
            result (pp/parse-header yaml-string)]
        (is (= {} result))))))
