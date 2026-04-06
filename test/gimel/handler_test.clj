(ns gimel.handler-test
  (:require [clojure.test :refer :all]
            [gimel.handler :as handler]))

(deftest wrap-html-content-type-test
  (testing "wrap-html-content-type middleware"
    (let [mock-handler (fn [_] {:status 200 :headers {} :body "body"})
          app (handler/wrap-html-content-type mock-handler)]
      (testing "for .html request"
        (let [response (app {:uri "/index.html"})]
          (is (= "text/html" (get-in response [:headers "Content-Type"])))))
      (testing "for non-.html request"
        (let [response (app {:uri "/style.css"})]
          (is (nil? (get-in response [:headers "Content-Type"]))))))))
