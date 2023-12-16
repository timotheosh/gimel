(ns gimel.partial-pages
  (:require [clojure.string :as string]
            [cybermonday.core :as cm]
            [gimel.config :as config]
            [gimel.templates :as tmpl]
            [gimel.database :as db]))

(def public-conf (:public (:configuration @(config/read-config))))
(def footer (:footer public-conf))

(defn partial-page-layout
  [page]
  (string/join (tmpl/public-page
                {:title (:title page)
                 :text (:html page)
                 :navbar (:navbar page)
                 :footer footer})))

(defn partial-pages
  ([pages] (partial-pages pages {}))
  ([pages navbar]
   (into {}
         (for [[key value] pages]
           (when (not (= key "/navbar.html"))
             (let [page-name key
                   strings (remove empty? (string/split value #"---" 3))
                   frontmatter (cm/parse-yaml (first strings))
                   body (second strings)]
               (db/insert-data page-name frontmatter body)
               {page-name #(partial-page-layout {:page page-name
                                                 :metadata frontmatter
                                                 :title (:title frontmatter)
                                                 :html body
                                                 :navbar navbar})}))))))
