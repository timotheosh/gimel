(ns gimel.static-pages
  (:require [clojure.string :as string]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [hiccup.core :refer [html]]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [markdown.transformers :refer [transformer-vector]]
            [gimel.config :as config]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]
            [gimel.static-files :refer [copy-files]]))

(def public-conf (:public (:configuration @(config/read-config))))
(def source-dir (:source-dir public-conf))
(def webroot (:webroot public-conf))

(defn convert-md-links [text state]
  (let [protocol-regex #".*\([^\)]*://.*\).*"
        md-link-regex  #"\(([^)]+?)\.md\)"]
    (if (re-find protocol-regex text)
      [text state] ; Skip alteration if it's an external URL
      [(string/replace text md-link-regex "($1.html)") state]))) ; Replace .md with .html for internal links

(defn preserve-spaces-in-links [text state]
  (let [space-regex #"\(([^)]+?)\s+([^)]+?)\)"]
    [string/replace text space-regex "($1%20$2)" state]))

(defn page-layout
  [request page]
  (string/join (tmpl/public-page
                {:text page
                 :navbar (html [:h1 "HEAD"])
                 :left-side (html [:h2 "SIDENOTES"])})))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(fn [req] (page-layout req %)) (vals pages))))

(defn markdown-pages [pages]
  (zipmap (map #(string/replace % #"\.md$" ".html") (keys pages))
          (map #(fn [req] (page-layout
                           req (md/md-to-html-string % :replacement-transformers
                                                     (into [convert-md-links] transformer-vector)))) (vals pages))))

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

(defn export []
  (let [assets (optimizations/all (tmpl/get-assets) {})]
    (stasis/empty-directory! webroot)
    (optimus.export/save-assets assets webroot)
    (stasis/export-pages (get-pages) webroot {:optimus-assets assets})
    (copy-files source-dir webroot [".jpg" ".png" ".gif" ".webp"])))
