(ns gimel.web
  (:require [clojure.string :as str]
            [bidi.bidi :refer [match-route]]
            [bidi.ring :refer [files make-handler]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [optimus.link :as link]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [ring.util.response :as res]
            [gimel.config :as config]
            [gimel.templates :as tmpl]
            [gimel.highlight :as highlight]))

(def public-conf (:public (:configuration @(config/read-config))))
(def admin-conf (:admin (:configuration @(config/read-config))))
(def public-dir (config/get-path (:document-dir public-conf)))

(defn response
  "Return the page/data sent wrapped in a ring response."
  [page]
  (res/content-type
   (res/response page)
   "text/html"))

(defn page-layout
  [request page]
  (clojure.string/join (tmpl/public-page
                        {:text page
                         :navbar (html [:h1 "HEADAE"])
                         :left-side (html [:h2 "SIDENOTES"])})))

(defn admin-layout
  [request page]
  (clojure.string/join (tmpl/admin-page
                        {:text page})))

(defn index-handler
  [request]
  (response (page-layout request (html [:h1 "Main Page"]))))

(defn admin-handler
  [request]
  (response (admin-layout request (html [:h1 "Admin Page"]))))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(fn [req] (response (page-layout req %)))
               (vals pages))))

(defn markdown-pages [pages]
  (zipmap (map #(str/replace % #"\.md$" ".html") (keys pages))
          (map #(fn [req] (response (page-layout req (md/md-to-html-string %)))) (vals pages))))

(defn get-raw-pages []
  (stasis/merge-page-sources
   {:public (stasis/slurp-directory public-dir #".*\.(css|js)$")
    :partials (partial-pages (stasis/slurp-directory public-dir #".*\.html$"))
    :markdown (markdown-pages (stasis/slurp-directory public-dir #".*\.md$"))}))

(defn prepare-page [page req]
  (-> (if (string? page) page (page req))
      highlight/highlight-code-blocks))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(partial prepare-page %) (vals pages))))

(defn serve-pages [request]
  (into [] (prepare-pages (get-raw-pages))))

(def routes ["/" [["" serve-pages]
                  ["admin" admin-handler]]])

(def handler
  (make-handler routes))
