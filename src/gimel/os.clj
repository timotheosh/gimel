(ns gimel.os
  (:require [clojure.string :as string]))

(defn path-append [& paths]
  (-> paths
      (#(clojure.string/join "/" %))
      (clojure.string/replace #"[\\/]+" "/")))

(defn dirname [path]
  (if-let [idx (string/last-index-of path "/")]
    (if (zero? idx)
      "/"
      (subs path 0 idx))
    "."))

(defn basename [path]
  (if-let [idx (string/last-index-of path "/")]
    (subs path (inc idx))
    path))
