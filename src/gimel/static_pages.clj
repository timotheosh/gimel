(ns gimel.static-pages
  (:require [clojure.string :as str]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [hiccup.core :refer [html]]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [gimel.config :as config]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]))

(def public-conf (:public (:configuration @(config/read-config))))
(def source-dir (config/get-path (:source-dir public-conf)))
(def webroot (config/get-path (:webroot (:public (:configuration @(config/read-config))))))
(def staging (config/get-path (:stage (:public (:configuration @(config/read-config))))))

(defn page-layout
  [request page]
  (clojure.string/join (tmpl/public-page
                        {:text page
                         :navbar (html [:h1 "HEADAE"])
                         :left-side (html [:h2 "SIDENOTES"])})))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(fn [req] (page-layout req %)) (vals pages))))

(defn markdown-pages [pages]
  (zipmap (map #(str/replace % #"\.md$" ".html") (keys pages))
          (map #(fn [req] (page-layout req (md/md-to-html-string %))) (vals pages))))

(defn get-raw-pages []
  (stasis/merge-page-sources
   {:public (stasis/slurp-directory source-dir #".*\.(css|js)$")
    :partials (partial-pages (stasis/slurp-directory source-dir #".*\.html$"))
    :markdown (markdown-pages (stasis/slurp-directory source-dir #".*\.md$"))}))

(defn prepare-page [page req]
  (-> (if (string? page) page (page req))
      highlight/highlight-code-blocks))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(partial prepare-page %) (vals pages))))

(defn get-pages []
  (prepare-pages (get-raw-pages)))

(defn serve-pages
  []
  (stasis/serve-pages get-pages))

(defn export []
  (let [assets (optimizations/all (tmpl/get-assets) {})]
    (stasis/empty-directory! webroot)
    (optimus.export/save-assets assets webroot)
    (stasis/export-pages (get-pages) webroot {:optimus-assets assets})))
