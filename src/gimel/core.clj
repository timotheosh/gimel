(ns gimel.core
  (:require [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.cli :refer [parse-opts]]
            [gimel.config :refer [load-config get-config]]
            [gimel.watcher :refer [start-watcher]]
            [gimel.api.core :refer [create-handler]])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-c" "--config CONFIGPATH" "Path to configfile"
    :default (str (System/getenv "HOME") "/.config/gimel/gimel.edn")
    :id :configfile]
   ["-h" "--help"]])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [options (parse-opts args cli-options)]
    (load-config (:configfile (:options options)))
    (let [config (get-config)
          webroot (:webroot (:public (:configuration config)))
          app (-> (create-handler)
                  (wrap-defaults site-defaults)
                  (wrap-file webroot))]
      (defonce server (jetty/run-jetty
                       app
                       {:port (or (:port (:options options)) (:port (:public (:configuration config))) 3000)
                        :send-server-version? false
                        :join? false})))))
