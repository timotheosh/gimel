(ns gimel.watcher
  (:require [clojure.java.io :as io]
            [juxt.dirwatch :refer [watch-dir]]
            [gimel.static-pages :refer [source-dir export]]))

(def watcher (atom nil))

(defn start-watcher []
  (export)
  (reset! watcher
          (watch-dir
           (fn [event] (export))
           (io/file source-dir))))
