(ns gimel.metadata-test
  (:require [clojure.test :refer [deftest is testing]]
            [gimel.metadata :refer [generate-navmenu]]))

(deftest generate-navmenu-exists-test
  (testing "generate-navmenu function exists in gimel.metadata namespace"
    (is (some? (resolve 'gimel.metadata/generate-navmenu)))
    (is (nil? (generate-navmenu {})))))
