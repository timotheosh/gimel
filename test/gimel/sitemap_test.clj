(ns gimel.sitemap-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [gimel.sitemap :refer [path->url file-data]]
            [gimel.config :as config]))

;; ---------------------------------------------------------------------------
;; Fixtures — temp file + stubbed config accessors
;; ---------------------------------------------------------------------------

(def ^:dynamic *temp-file* nil)

(defn sitemap-fixture [f]
  (let [tmp (java.io.File/createTempFile "sitemap-test" ".md")]
    (binding [*temp-file* tmp]
      (with-redefs [config/get-org-source (fn [] (.getParent tmp))
                    config/get-web-url         (fn [] "https://example.com")]
        (f)))
    (.delete tmp)))

(use-fixtures :each sitemap-fixture)

;; ---------------------------------------------------------------------------
;; Unit tests
;; ---------------------------------------------------------------------------

(deftest test-path->url-md-to-html
  (testing "path->url converts .md extension to .html"
    (let [f (java.io.File/createTempFile "page" ".md")]
      (try
        (with-redefs [config/get-org-source (fn [] (.getParent f))
                      config/get-web-url         (fn [] "https://example.com")]
          (let [url (path->url f)]
            (is (clojure.string/ends-with? url ".html"))))
        (finally (.delete f))))))

(deftest test-path->url-org-to-html
  (testing "path->url converts .org extension to .html"
    (let [f (java.io.File/createTempFile "page" ".org")]
      (try
        (with-redefs [config/get-org-source (fn [] (.getParent f))
                      config/get-web-url         (fn [] "https://example.com")]
          (let [url (path->url f)]
            (is (clojure.string/ends-with? url ".html"))))
        (finally (.delete f))))))

(deftest test-file-data-keys
  (testing "file-data returns a map with :loc, :lastmod, and :changefreq keys"
    (let [data (file-data *temp-file*)]
      (is (map? data))
      (is (contains? data :loc))
      (is (contains? data :lastmod))
      (is (contains? data :changefreq)))))

(deftest test-file-data-loc-starts-with-https
  (testing ":loc value starts with https://"
    (let [data (file-data *temp-file*)]
      (is (clojure.string/starts-with? (:loc data) "https://")))))
