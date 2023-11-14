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
            [gimel.highlight :as highlight]))

(def public-conf (:public (:configuration @(config/read-config))))
(def source-dir (:source-dir public-conf))
(def webroot (:webroot public-conf))

(defn escape-images [text state]
  [(string/replace text #"(!\[.*?\]\()(.+?)(\))" "") state])

(defn escape-html
  "Change special characters into HTML character entities."
  [text state]
  [(if-not (or (:code state) (:codeblock state))
     (string/escape
      text
      {\& "&amp;"
       \< "&lt;"
       \> "&gt;"
       \" "&quot;"
       \' "&#39;"})
     text) state])

(defn convert-md-links [text state]
  (let [protocol-regex #".*\([^\)]*://.*\).*"
        md-link-regex  #"\(([^)]+?)\.md\)"]
    (if (re-find protocol-regex text)
      [text state] ; Skip alteration if it's an external URL
      [(string/replace text md-link-regex "($1.html)") state]))) ; Replace .md with .html for internal links

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
                                                     (into [escape-images escape-html convert-md-links] transformer-vector)))) (vals pages))))

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
