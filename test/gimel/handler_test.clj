(ns gimel.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [gimel.handler :refer [wrap-html-content-type]]))

(deftest wrap-html-content-type-sets-html-header
  (testing "sets Content-Type to text/html for .html URIs"
    (let [handler (wrap-html-content-type (fn [_req] {:status 200 :headers {} :body ""}))
          response (handler {:uri "/page.html"})]
      (is (= "text/html" (get-in response [:headers "Content-Type"]))))))

(deftest wrap-html-content-type-leaves-other-unchanged
  (testing "leaves Content-Type unchanged for non-.html URIs"
    (let [original-ct "application/json"
          handler (wrap-html-content-type (fn [_req] {:status 200 :headers {"Content-Type" original-ct} :body ""}))
          response (handler {:uri "/api/data"})]
      (is (= original-ct (get-in response [:headers "Content-Type"]))))))

(deftest wrap-html-content-type-passes-request-through
  (testing "passes the original request unchanged to the wrapped handler"
    (let [captured (atom nil)
          request {:uri "/page.html" :method :get :params {:foo "bar"}}
          handler (wrap-html-content-type (fn [req]
                                            (reset! captured req)
                                            {:status 200 :headers {} :body ""}))
          _ (handler request)]
      (is (= request @captured)))))
