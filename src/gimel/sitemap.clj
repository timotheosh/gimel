(ns gimel.sitemap
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [tick.core :as tick]
            [sitemap.core :refer [generate-sitemap]]
            [gimel.config :as config]
            [gimel.os :refer [path-append]]))

(def public-conf (:public (:configuration @(config/read-config))))
(def source-dir (:source-dir public-conf))
(def webroot (:webroot public-conf))
(def web-url (:web-url public-conf))

(defn path->url [io-file]
  (-> (.getAbsolutePath io-file)
      (string/replace (re-pattern source-dir) "")
      (string/replace #"\.md$" ".html")
      ((fn [x] (path-append web-url x)))))

(defn file-data
  "Returns relevant file data for generating a sitemap."
  [io-file]
  {:loc (path->url io-file)
   :lastmod (str (tick/date (java.util.Date. (.lastModified io-file))))
   :changefreq "monthly"})

(defn gen-sitemap []
  (let [grammar-matcher (.getPathMatcher
                         (java.nio.file.FileSystems/getDefault)
                         "glob:*.{pdf,md}")]
    (->> source-dir
         io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(file-data %))
         generate-sitemap
         (spit (str (path-append webroot "/sitemap.xml"))))))
