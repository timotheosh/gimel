(ns gimel.static-pages
  (:require [clojure.java.io :as io]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [stasis.core :as stasis]
            [gimel.config :refer [get-config get-source-dir get-webroot get-sitemap-source]]
            [gimel.os :as os]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]
            [gimel.markdown :refer [markdown-pages]]
            [gimel.partial-pages :refer [partial-pages]]
            [gimel.static-files :refer [copy-files]]
            [gimel.database :refer [create-database]]
            [gimel.sitemap :refer [gen-sitemap]]))

(defn get-navbar-html
  "Gets a pre-exisitng navigation bar in an html file, and returns it as a string."
  [source]
  (let [file (io/file (os/path-append source "navbar.html"))]
    (if (.exists file)
      (slurp file))))

(defn get-raw-pages [source]
  (stasis/merge-page-sources
   {:public (stasis/slurp-directory source #".*\.(css|js)$")
    :partials (partial-pages (stasis/slurp-directory source #".*\.html$") (get-navbar-html source))
    :markdown (markdown-pages (stasis/slurp-directory source #".*\.md$") (get-navbar-html source))}))

(defn prepare-page [page]
  (-> (if (string? page) page (page))
      highlight/highlight-code-blocks))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(prepare-page %) (vals pages))))

(defn get-pages [source]
  (prepare-pages (get-raw-pages source)))

(defn export
  "Wipes public directory and recreates website from source to public."
  ([] (export (get-source-dir) (get-webroot) (get-sitemap-source)))
  ([source public] (export source public (get-sitemap-source)))
  ([source public sitemap]
   (create-database)
   (let [assets (optimizations/all (tmpl/get-assets) {})]
     (stasis/empty-directory! public)
     (optimus.export/save-assets assets public)
     (stasis/export-pages (get-pages source) public {:optimus-assets assets})
     (copy-files source public [".jpg" ".png" ".gif" ".webp" ".js" ".css"])
     (gen-sitemap sitemap public))))
