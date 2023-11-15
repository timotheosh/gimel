(ns gimel.os
  (:require [clojure.string :as string]))

(defn path-append [& paths]
  (-> paths
      (#(clojure.string/join "/" %))
      (clojure.string/replace ,  #"[\\/]+" "/")))

(defn dirname [path]
  (subs path 0 (string/last-index-of path "/")))

(defn basename [path]
  (subs path (inc (string/last-index-of path "/"))))
