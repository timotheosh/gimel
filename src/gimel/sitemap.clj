(ns gimel.sitemap
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [tick.core :as tick]
            [sitemap.core :refer [generate-sitemap]]
            [gimel.config :refer [get-source-dir get-web-url]]
            [gimel.os :refer [path-append]]))


(defn path->url [io-file]
  (-> (.getAbsolutePath io-file)
      (string/replace (re-pattern (get-source-dir)) "")
      (string/replace #"\.md$" ".html")
      (string/replace #"\.org$" ".html")
      ((fn [x] (path-append (get-web-url) x)))))

(defn file-data
  "Returns relevant file data for generating a sitemap."
  [io-file]
  {:loc (path->url io-file)
   :lastmod (str (tick/date (java.util.Date. (.lastModified io-file))))
   :changefreq "monthly"})

(defn gen-sitemap
  [source web-url]
  (let [grammar-matcher (.getPathMatcher
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{pdf,md,org,html}")]
    (->> source
         io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(file-data %))
         generate-sitemap
         (spit (str (path-append web-url "/sitemap.xml"))))))
