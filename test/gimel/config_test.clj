(ns gimel.config-test
  (:require [clojure.test :refer [deftest is use-fixtures testing]]
            [clojure.java.io :as io]
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
    (is (= "/etc/gimel.toml" (config/expand-home "/etc/gimel.toml")))))

(deftest load-config-nonexistent-path-throws-exception
  (testing "a non-existent path causes an exception to be thrown"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Config file does not exist"
                          (config/load-config "/this/path/does/not/exist/gimel.toml")))))

(deftest get-config-returns-non-empty-map
  (testing "get-config returns a non-empty map after load-config with valid config"
    (config/load-config "resources/config/gimel.toml")
    (let [cfg (config/get-config)]
      (is (map? cfg))
      (is (pos? (count cfg))))))

(deftest get-config-throws-on-blank-atom
  (testing "get-config throws ex-info when config-data atom is empty"
    (is (thrown? clojure.lang.ExceptionInfo (config/get-config)))))

(deftest load-config-toml-fixture
  (testing "loading resources/config/gimel.toml returns expected accessor values"
    (config/load-config "resources/config/gimel.toml")
    (is (= 8080 (config/get-port)))
    (is (= "https://naurrnen.selfdidactic.com" (config/get-web-url)))
    (is (= "resources/public" (config/get-webroot)))
    (is (= "resources/html" (config/get-snippet-output)))
    (is (= "gimel.db" (config/get-dbname)))))
