(ns gimel.markdown
  (:require [clojure.string :as string]
            [clojure.walk :refer [postwalk]]
            [hiccup.core :refer [html]]
            [markdown.core :as md]
            [markdown.transformers :refer [transformer-vector]]
            [cybermonday.ir :as ir]
            [cybermonday.core :as cm]
            [cybermonday.utils :refer [gen-id make-hiccup-node]]
            [gimel.config :refer [get-footer]]
            [gimel.templates :as tmpl]
            [gimel.database :as db]))

(defn flexmark-headers
  "Cybermonkey lowering function that autocreates anchor links for all headers."
  [[_ attrs & body :as node]]
  (make-hiccup-node
   :div
   (dissoc
    (let [id (if (nil? (:id attrs))
               (gen-id node)
               (:id attrs))]
      (assoc attrs
             :id id
             :class "anchor"))
    :level)
   [(make-hiccup-node
     (keyword (str "h" (:level attrs)))
     (if (:class attrs)
       {:class (:class attrs)}
       {})
     body)]))

(defn flexmark-filter
  "Postwalk hiccup filters for changing link references to html, and
  handling markdown images that are encased between html tags."
  [element]
  (cond (and (map? element) (:href element) (not (re-find #"[\w]+:\/\/" (:href element))))
        (let [combined-regex #"([^ ]+?)\.md(?=$|\s|#)"]
          (update element :href #(string/replace % combined-regex "$1.html")))
        (and (string? element) (re-find #"\!\[[^\]]+\]\([^\)]+\)" element))
        (let [[_ _ [_ _ image]] (ir/md-to-ir element)] image)
        :else element))

(defn mdclj-convert-md-links
  "Convert internet md links to html links for markdown-clj parser."
  [text state]
  (let [combined-regex #"\((?![^)]*:\/\/)([^)]+?)\.md([)#])"]
    [(string/replace text combined-regex "($1.html$2") state]))

(defn md-page-layout
  [page]
  (tmpl/render-public-page
   {:title (:title page)
    :text (:html page)
    :navbar (:navbar page)
    :footer (get-footer)}))

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
             (db/insert-data page-name (:frontmatter processed-page) (:body processed-page))
             {page-name #(md-page-layout {:page page-name
                                          :metadata (:frontmatter processed-page)
                                          :title (:Title (:frontmatter processed-page))
                                          :html (:body processed-page)
                                          :navbar navbar})})))))
