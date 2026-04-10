(ns gimel.api-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]
            [gimel.api.core :refer [create-api-handler valid-data?]]
            [gimel.static-pages :as pages]))

(def handler (create-api-handler))

(deftest test-get-export-returns-200
  (testing "GET /api/export returns HTTP 200"
    (with-redefs [pages/export (fn
                                 ([] nil)
                                 ([_ _ _] nil))]
      (let [response (handler (mock/request :get "/api/export"))]
        (is (= 200 (:status response)))))))

(deftest test-post-export-custom-invalid-body-returns-400
  (testing "POST /api/export-custom with invalid body returns HTTP 400"
    (let [req (-> (mock/request :post "/api/export-custom")
                  (mock/content-type "application/json")
                  (mock/body "{\"bad\": \"data\"}"))
          response (handler req)]
      (is (= 400 (:status response))))))

(deftest test-undefined-route-returns-404-with-error
  (testing "undefined route returns 404 with error JSON"
    (let [response (handler (mock/request :get "/api/does-not-exist"))]
      (is (= 404 (:status response)))
      (is (re-find #"error" (:body response))))))

(deftest test-valid-data-falsy-when-paths-missing
  (testing "valid-data? returns falsy when required directory paths are missing"
    (is (not (valid-data? {:source "/no/such/path" :public "/also/missing"})))
    (is (not (valid-data? {:source nil :public nil})))))
