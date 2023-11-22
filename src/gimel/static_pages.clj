(ns gimel.static-pages
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.walk :refer [postwalk prewalk]]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [hiccup.core :refer [html]]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [markdown.transformers :refer [transformer-vector]]
            [cybermonday.ir :as ir]
            [cybermonday.core :as cm]
            [cybermonday.utils :refer [gen-id make-hiccup-node]]
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

(defn extract-img [element]
  (if (and (vector? element) (= (first element) :div))
    (let [[_ _ [_ _ img]] element]
      img)
    element))

(defn flexmark-headers
  "Autocreates anchor links for all headers."
  [[_ attrs & body :as node]]
  (make-hiccup-node
   :div
   (dissoc
    (let [id (if (nil? (:id attrs))
               (gen-id node)
               (:id attrs))]
      (assoc attrs
             :id id
             :class "anchor"
             :href (str "#" id)))
    :level)
   [(make-hiccup-node
     (keyword (str "h" (:level attrs)))
     (dissoc
      (let [id (if (nil? (:id attrs))
                 (gen-id node)
                 (:id attrs))]
        (assoc attrs
               :id id))
      :level)
     body)]))

(defn flexmark-filter [element]
  (cond (and (map? element) (:href element)) (let [combined-regex #"(?<!\S:\/\/)([^ ]+?)\.md(?=$|\s|#)"]
                                               (update element :href #(clojure.string/replace % combined-regex "$1.html")))
        (and (string? element) (or (re-find #"\!\[[^\]]+\]\([^\)]+\)" element))) (extract-img (ir/md-to-ir element))
        :else element))

(defn mdclj-convert-md-links [text state]
  (let [combined-regex #"\((?![^)]*:\/\/)([^)]+?)\.md([)#])"]
    [(clojure.string/replace text combined-regex "($1.html$2") state]))

(defn mdclj-preserve-spaces-in-links [text state]
  (let [space-regex #"\(([^)]+?)\s+([^)]+?)\)"]
    [string/replace text space-regex "($1%20$2)" state]))

(defn md-page-layout
  [page]
  (string/join (tmpl/public-page
                {:title (:title page)
                 :text (:html page)
                 :navbar (:navbar page)
                 :footer footer})))

(defn page-layout
  [page]
  (string/join (tmpl/public-page
                {:text (:html page)
                 :navbar (html [:h1 "HEAD"])
                 :footer footer})))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map #(page-layout %) (vals pages))))

(defn process-markdown
  "Preprocesses the markdown file for cybermonday."
  [markdown-string]
  (let [strings (remove empty? (string/split markdown-string #"---" 3))
        frontmatter (cm/parse-yaml (first strings))]
    (if (= (:Processor frontmatter) "markdown-clj")
      {:frontmatter frontmatter :body (md/md-to-html-string
                                       (second strings) :replacement-transformers
                                       (into [mdclj-convert-md-links] transformer-vector))}
      {:frontmatter frontmatter :body
       (html (postwalk flexmark-filter (cm/parse-body (second strings)
                                                      {:lower-fns {:markdown/heading flexmark-headers}})))})))

(defn markdown-pages
  ([pages] (markdown-pages pages {}))
  ([pages navbar]
   (into {}
         (for [[key value] pages]
           (let [processed-page (process-markdown value)
                 page-name (string/replace key #"\.md$" ".html")]
             {page-name #(md-page-layout {:page page-name
                                          :metadata (:frontmatter processed-page)
                                          :title (:Title (:frontmatter processed-page))
                                          :html (:body processed-page)
                                          :navbar navbar})})))))

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
