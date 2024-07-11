(ns gimel.partial-pages
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [cybermonday.core :as cm]
            [gimel.config :refer [get-footer]]
            [gimel.templates :as tmpl]
            [gimel.database :as db]))

(defn parse-header
  "Parses yaml header of partial page for metadata."
  [data]
  (try
    (into {} (cm/parse-yaml data))
    (catch org.yaml.snakeyaml.scanner.ScannerException exc
      {:title "NO META DATA" :error "NO META DATA"})))

(defn partial-page-layout
  "Renders the layout for a partial page using the provided page data."
  [page]
  (tmpl/render-public-page
   {:title (:title page)
    :text (:html page)
    :navbar (:navbar page)
    :footer (get-footer)}))

(defn partial-pages
  "Processes a map of pages, parsing headers, and rendering layouts.
  Accepts an optional navbar map."
  ([pages] (partial-pages pages {}))
  ([pages navbar]
   (into {}
         (for [[key value] pages]
           (when (not (= key "/navbar.html"))
             (let [page-name key
                   strings (remove empty? (string/split value #"---" 3))
                   frontmatter (parse-header (first strings))
                   body (if (:error frontmatter)
                          (do
                            (log/warn (str "Page: " page-name " has no meta data!"))
                            (str "<h1 style=\"color: red;\">NO META DATA</h1>\n" value))
                          (second strings))]
               (db/insert-data page-name frontmatter body)
               {page-name #(partial-page-layout {:page page-name
                                                 :metadata frontmatter
                                                 :title (:title frontmatter)
                                                 :html body
                                                 :navbar navbar})}))))))
