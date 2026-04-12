(ns gimel.config-spec-test
  (:require [clojure.test :refer [deftest is testing]]
            [gimel.config-spec :refer [check-config]]))

(def valid-config
  {:configuration
   {:public {:org-source "/tmp/src"
             :snippet-output     "/tmp/src"
             :webroot        "/tmp/public"
             :template       "resources/templates/naurrnen-layout"
             :footer         "Footer text"
             :web-url        "https://example.com"
             :port           8080}
    :database {:dbname "/tmp/test.db"}}})

(deftest test-valid-config-returned-unchanged
  (testing "valid config is returned unchanged"
    (is (= valid-config (check-config valid-config)))))

(deftest test-missing-required-key-throws
  (testing "config missing a required key throws ex-info"
    (let [bad-config (update-in valid-config [:configuration :public] dissoc :webroot)]
      (is (thrown? clojure.lang.ExceptionInfo (check-config bad-config))))))

(deftest test-invalid-web-url-throws
  (testing "invalid :web-url throws ex-info"
    (let [bad-config (assoc-in valid-config [:configuration :public :web-url] "not-a-url")]
      (is (thrown? clojure.lang.ExceptionInfo (check-config bad-config))))))

(deftest test-non-positive-port-throws
  (testing "non-positive :port throws ex-info"
    (let [bad-config (assoc-in valid-config [:configuration :public :port] 0)]
      (is (thrown? clojure.lang.ExceptionInfo (check-config bad-config))))
    (let [bad-config (assoc-in valid-config [:configuration :public :port] -1)]
      (is (thrown? clojure.lang.ExceptionInfo (check-config bad-config))))))
