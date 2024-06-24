(ns gimel.watcher
  (:require [clojure.java.io :as io]
            [juxt.dirwatch :refer [watch-dir]]
            [gimel.config :refer [get-source-dir]]
            [gimel.static-pages :refer [export]]))

(def watcher (atom nil))

(defn start-watcher []
  (export)
  (reset! watcher
          (watch-dir
           (fn [event]
             (println event)
             (export))
           (io/file (get-source-dir)))))
