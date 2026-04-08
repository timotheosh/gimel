(ns gimel.highlight
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]))

(defn- highlight [node]
  (let [code (->> node :content (apply str))
        lang (->> node :attrs :class (apply str))]
    (assoc-in node [:attrs :class]
              (str "language-" lang
                   " line-numbers"))))

(defn highlight-code-blocks [page-html]
  (if-not (string/blank? page-html)
    (let [nodes (enlive/html-resource (java.io.StringReader. page-html))
          transformed-nodes (enlive/transform nodes [:pre :code] highlight)]
      (string/join (enlive/emit* transformed-nodes)))
    ""))
