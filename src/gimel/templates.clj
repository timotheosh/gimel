(ns gimel.templates
  (:require [net.cgrand.enlive-html :as html]
            [hiccup.core :as hiccup]
            [optimus.assets :as assets]
            [gimel.config :as config]))

(def public-conf (:public (:configuration @(config/read-config))))


(def public-template (config/get-file (:template public-conf) "index.html"))


(def public-template-config (config/read-edn (config/get-file (:template public-conf) "template.edn")))


(defn get-assets []
  (concat
   (assets/load-assets (clojure.string/replace
                        (:template public-conf)
                        #"^resources/" "") [#".*\.(css|js)$"])))

(html/deftemplate public-page public-template
  [ctxt]
  [:title] (html/html-content (:title ctxt))
  (:navbar (:body public-template-config)) (html/html-content (:navbar ctxt))
  (:main (:body public-template-config)) (html/html-content (:text ctxt))
  (:footer (:body public-template-config)) (html/html-content (:footer ctxt)))
