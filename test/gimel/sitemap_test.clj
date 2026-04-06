(ns gimel.sitemap-test
  (:require [clojure.test :refer :all]
            [gimel.sitemap :as sitemap]
            [clojure.java.io :as io]))

(deftest path->url-test
  (testing "path->url function"
    (with-redefs [gimel.config/get-sitemap-source (fn [] "/path/to/source")
                  gimel.config/get-web-url (fn [] "https://example.com")]
      (let [file (io/file "/path/to/source/blog/post.md")]
        (is (= "https://example.com/blog/post.html" (sitemap/path->url file))))
      (let [file (io/file "/path/to/source/about.org")]
        (is (= "https://example.com/about.html" (sitemap/path->url file))))
      (let [file (io/file "/path/to/source/index.html")]
        (is (= "https://example.com/index.html" (sitemap/path->url file)))))))
