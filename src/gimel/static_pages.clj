(ns gimel.static-pages
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [hiccup.core :refer [html]]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [markdown.transformers :refer [transformer-vector]]
            [gimel.config :as config]
            [gimel.os :as os]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]
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

(defn convert-md-links [text state]
  (let [combined-regex #"\((?![^)]*:\/\/)([^)]+?)\.md([)#])"]
    [(clojure.string/replace text combined-regex "($1.html$2") state]))

(defn preserve-spaces-in-links [text state]
  (let [space-regex #"\(([^)]+?)\s+([^)]+?)\)"]
    [string/replace text space-regex "($1%20$2)" state]))

(defn md-page-layout
  [request page]
  (string/join (tmpl/public-page
                {:title (first (:title (:metadata page)))
                 :text (:html page)
                 :navbar (:navbar page)
                 :footer footer})))

(defn page-layout
  [request page]
  (string/join (tmpl/public-page
                {:text (:html page)
                 :navbar (html [:h1 "HEAD"])
                 :footer footer})))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(fn [req] (page-layout req %)) (vals pages))))

(defn markdown-pages
  ([pages] (markdown-pages pages {}))
  ([pages navbar]
   (zipmap (map #(string/replace % #"\.md$" ".html") (keys pages))
           (map #(fn [req]
                   (md-page-layout
                    req (assoc (md/md-to-html-string-with-meta
                                % :replacement-transformers
                                (into [convert-md-links] transformer-vector))
                               :navbar navbar))) (vals pages)))))

(defn markdown-meta [pages]
  (zipmap (map #(string/replace % #"\.md$" ".html") (keys pages))
          (map (fn [page] (:metadata (md/md-to-html-string-with-meta page))) (vals pages))))

(defn get-raw-pages []
  (stasis/merge-page-sources
   {:public (stasis/slurp-directory source-dir #".*\.(css|js)$")
    :partials (partial-pages (stasis/slurp-directory source-dir #".*\.html$"))
    :markdown (markdown-pages (stasis/slurp-directory source-dir #".*\.md$") (get-navbar-html))}))

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
    (copy-files source-dir webroot [".jpg" ".png" ".gif" ".webp" ".js" ".css"])))

(defn old-export []
  (let [assets (optimizations/all (tmpl/get-assets) {})]
    (stasis/empty-directory! webroot)
    (optimus.export/save-assets assets webroot)
    (stasis/export-pages (get-pages) webroot {:optimus-assets assets})
    (copy-files source-dir webroot [".jpg" ".png" ".gif" ".webp"])))
