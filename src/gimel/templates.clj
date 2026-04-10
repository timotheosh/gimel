(ns gimel.templates
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [optimus.assets :as assets]
            [gimel.config :refer [get-file read-edn get-template-dir]]))

(defn get-public-template []
  (get-file (get-template-dir) "index.html"))

(defn get-public-template-config []
  (read-edn (get-file (get-template-dir) "template.edn")))

(defn get-assets []
  (concat
   (assets/load-assets (string/replace
                        (get-template-dir)
                        #"^resources/" "") [#".*\.(css|js)$"])))

(defn render-public-page [ctxt]
  (let [public-template (get-public-template)
        public-template-config (get-public-template-config)
        template-fn (html/template public-template
                      [ctxt]
                      [:title] (html/html-content (:title ctxt))
                      (:navbar (:body public-template-config)) (html/html-content (:navbar ctxt))
                      (:main (:body public-template-config)) (html/html-content (:text ctxt))
                      (:footer (:body public-template-config)) (html/html-content (:footer ctxt)))]
    (string/join (template-fn ctxt))))
