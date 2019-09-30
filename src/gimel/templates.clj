(ns gimel.templates
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            [hiccup.core :as hiccup]
            [optimus.assets :as assets]
            [gimel.config :as config]))

(def public-conf (:public (:configuration @(config/read-config))))
(def admin-conf (:admin (:configuration @(config/read-config))))

(def public-template (config/get-file (:template public-conf) "index.html"))
(def admin-template (config/get-file (:template admin-conf) "index.html"))

(def public-template-config (config/read-edn (config/get-file (:template public-conf) "template.edn")))
(def admin-template-config (config/read-edn (config/get-file (:template admin-conf) "template.edn")))

(defn get-assets []
  (concat
   (assets/load-assets (config/get-path (:template admin-conf)) [#".*\.(css|js)$"])
   ))


(def admin-navbar
  (hiccup/html [:ul
                [:li [:a {:href "/"} "Home"]]
                [:li [:a {:href "/admin"} "Admin"]]]))

(html/deftemplate public-page public-template
  [ctxt]
  (:header (:body public-template-config)) (html/html-content (:navbar ctxt))
  (:main (:body public-template-config)) (html/html-content (:text ctxt))
  (:left-side (:body public-template-config)) (html/html-content (:left-side ctxt))
  (:right-side (:body public-template-config)) (html/html-content (:right-side ctxt))
  (:footer (:body public-template-config)) (html/html-content (:footer ctxt)))

(html/deftemplate admin-page admin-template
  [ctxt]
  (:header (:body admin-template-config)) (html/html-content admin-navbar)
  (:main (:body admin-template-config)) (html/html-content (:text ctxt))
  (:left-side (:body admin-template-config)) (html/html-content (:left-side ctxt))
  (:right-side (:body admin-template-config)) (html/html-content (:right-side ctxt))
  (:footer (:body admin-template-config)) (html/html-content (:footer ctxt)))
