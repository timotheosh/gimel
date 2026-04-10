(ns gimel.config-test
  (:require [clojure.test :refer [deftest is use-fixtures testing]]
            [gimel.config :as config]))

(defn reset-config-fixture [f]
  (reset! config/config-data {})
  (f)
  (reset! config/config-data {}))

(use-fixtures :each reset-config-fixture)

(deftest expand-home-tilde-prefix
  (testing "~ is replaced with the user home directory"
    (let [home (System/getProperty "user.home")
          result (config/expand-home "~/some/path")]
      (is (= (str home "/some/path") result)))))

(deftest expand-home-plain-path
  (testing "a path without ~ is returned unchanged"
    (is (= "/etc/gimel.edn" (config/expand-home "/etc/gimel.edn")))))

(deftest load-config-nonexistent-path-loads-default
  (testing "a non-existent path causes the bundled default config to be loaded"
    (config/load-config "/this/path/does/not/exist/gimel.edn")
    (let [cfg (config/get-config)]
      (is (map? cfg))
      (is (contains? cfg :configuration)))))

(deftest get-config-returns-non-empty-map
  (testing "get-config returns a non-empty map after load-config"
    (config/load-config "/this/path/does/not/exist/gimel.edn")
    (let [cfg (config/get-config)]
      (is (map? cfg))
      (is (pos? (count cfg))))))

(deftest get-config-throws-on-blank-atom
  (testing "get-config throws ex-info when config-data atom is empty"
    (is (thrown? clojure.lang.ExceptionInfo (config/get-config)))))
