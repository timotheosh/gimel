(ns gimel.highlight
  (:require [net.cgrand.enlive-html :as enlive]))

(defn- highlight [node]
  (let [lang (->> node :attrs :class (apply str))]
    (assoc-in node [:attrs :class]
              (str "language-" lang
                   " line-numbers"))))

(defn highlight-code-blocks [page]
  (enlive/sniptest page
                   [:pre :code] highlight))
