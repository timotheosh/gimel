(ns gimel.static-pages
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [optimus.export]
            [optimus.optimizations :as optimizations]
            [hiccup.core :refer [html]]
            [stasis.core :as stasis]
            [gimel.config :as config]
            [gimel.os :as os]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]
            [gimel.markdown :refer [markdown-pages]]
            [gimel.static-files :refer [copy-files]]))

(def public-conf (:public (:configuration @(config/read-config))))
(def source-dir (:source-dir public-conf))
(def webroot (:webroot public-conf))
(def footer (:footer public-conf))


(defn get-navbar-html
  "Gets a pre-exisitng navigation bar in an html file, and returns it as a string."
  []
  (let [file (io/file (os/path-append source-dir "navbar.html"))]
    (if (.exists file)
      (slurp file))))

(defn page-layout
  [page]
  (string/join (tmpl/public-page
                {:text (:html page)
                 :navbar (html [:h1 "HEAD"])
                 :footer footer})))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(page-layout %) (vals pages))))

(defn get-raw-pages []
  (stasis/merge-page-sources
   {:public (stasis/slurp-directory source-dir #".*\.(css|js)$")
    :partials (partial-pages (stasis/slurp-directory source-dir #".*\.html$"))
    :markdown (markdown-pages (stasis/slurp-directory source-dir #".*\.md$") (get-navbar-html))}))

(defn prepare-page [page]
  (-> (if (string? page) page (page))
      highlight/highlight-code-blocks))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(prepare-page %) (vals pages))))

(defn get-pages []
  (prepare-pages (get-raw-pages)))

(defn export []
  (let [assets (optimizations/all (tmpl/get-assets) {})]
    (stasis/empty-directory! webroot)
    (optimus.export/save-assets assets webroot)
    (stasis/export-pages (get-pages) webroot {:optimus-assets assets})
    (copy-files source-dir webroot [".jpg" ".png" ".gif" ".webp" ".js" ".css"])))

(defn old-export []
  (let [assets (optimizations/all (tmpl/get-assets) {})]
    (stasis/empty-directory! webroot)
    (optimus.export/save-assets assets webroot)
    (stasis/export-pages (get-pages) webroot {:optimus-assets assets})
    (copy-files source-dir webroot [".jpg" ".png" ".gif" ".webp"])))
