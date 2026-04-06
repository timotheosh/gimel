(ns gimel.config-test
  (:require [clojure.test :refer :all]
            [gimel.config :as config]))

(deftest expand-home-test
  (testing "expand-home function"
    (let [original-home (System/getProperty "user.home")]
      (try
        (System/setProperty "user.home" "/testhome")
        (is (= "/testhome/some/path" (config/expand-home "~/some/path")))
        (is (= "/some/other/path" (config/expand-home "/some/other/path")))
        (is (= "nottilde/path" (config/expand-home "nottilde/path")))
        (finally
          (System/setProperty "user.home" original-home))))))
