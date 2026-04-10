(ns gimel.os
  (:require [clojure.string :as string]))

(defn path-append [& paths]
  (let [filtered (remove #(or (nil? %) (empty? %)) paths)
        joined   (string/join "/" filtered)]
    (string/replace joined #"[\\/]+" "/")))

(defn dirname [path]
  (let [idx (string/last-index-of path "/")]
    (if (nil? idx) "." (subs path 0 idx))))

(defn basename [path]
  (let [idx (string/last-index-of path "/")]
    (if (nil? idx) path (subs path (inc idx)))))
