(ns gimel.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]
            [gimel.api.core :refer [create-api-handler valid-data?]]
            [gimel.static-pages :as static-pages]))

;; Stub export to avoid real filesystem side effects
(defn stub-export
  ([] nil)
  ([_source _public _sitemap] nil))

(use-fixtures :each
  (fn [f]
    (with-redefs [static-pages/export stub-export]
      (f))))

(deftest test-get-export-returns-200
  (testing "GET /api/export returns HTTP 200"
    (let [handler (create-api-handler)
          request (mock/request :get "/api/export")
          response (handler request)]
      (is (= 200 (:status response))))))

(deftest test-post-export-custom-invalid-body-returns-400
  (testing "POST /api/export-custom with invalid body returns HTTP 400"
    (let [handler (create-api-handler)
          ;; Send a request with no valid source/public/org-path directories
          request (-> (mock/request :post "/api/export-custom")
                      (mock/content-type "application/json")
                      (mock/body "{\"source\":\"/nonexistent-path\",\"public\":\"/also-nonexistent\"}"))
          response (handler request)]
      (is (= 400 (:status response))))))

(deftest test-undefined-route-returns-404-with-error
  (testing "Request to undefined route returns HTTP 404 with JSON error body"
    (let [handler (create-api-handler)
          request (mock/request :get "/api/undefined-route")
          response (handler request)]
      (is (= 404 (:status response)))
      (is (re-find #"error" (or (:body response) ""))))))

(deftest test-valid-data?-falsy-when-paths-missing
  (testing "valid-data? returns falsy when required directory paths are missing"
    (let [context {:request {:source "/nonexistent-source"
                             :public "/nonexistent-public"
                             :org-path "/nonexistent-org"}}
          result (valid-data? context)]
      (is (not (map? (get result :export-data)))))))
