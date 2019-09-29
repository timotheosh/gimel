(ns gimel.highlight
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as enlive]))

(defn- highlight [node]
  (let [code (->> node :content (apply str))
        lang (->> node :attrs :class (apply str))]
    (assoc-in node [:attrs :class]
              (str "language-" lang
                   " line-numbers"))))

(defn highlight-code-blocks [page]
  (enlive/sniptest page
                   [:pre :code] highlight))
