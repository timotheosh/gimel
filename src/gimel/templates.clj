(ns gimel.templates
  (:require [net.cgrand.enlive-html :as html]
            [hiccup.core :as hiccup]
            [optimus.assets :as assets]
            [gimel.config :refer [get-file read-edn get-template-dir]]))

(defn get-public-template []
  (get-file (get-template-dir) "index.html"))

(defn get-public-template-config []
  (read-edn (get-file (get-template-dir) "template.edn")))

(defn get-assets []
  (concat
   (assets/load-assets (clojure.string/replace
                        (get-template-dir)
                        #"^resources/" "") [#".*\.(css|js)$"])))

(defn render-public-page [ctxt]
  (let [public-template (get-public-template)
        public-template-config (get-public-template-config)]
    (html/deftemplate public-page public-template
      [ctxt]
      [:title] (html/html-content (:title ctxt))
      (:navbar (:body public-template-config)) (html/html-content (:navbar ctxt))
      (:main (:body public-template-config)) (html/html-content (:text ctxt))
      (:footer (:body public-template-config)) (html/html-content (:footer ctxt)))
    (clojure.string/join (public-page ctxt))))
