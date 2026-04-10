(ns gimel.core
  (:require [gimel.api.core :refer [create-api-handler]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.adapter.jetty :as jetty]
            [mount.core :as mount]
            [clojure.tools.cli :refer [parse-opts]]
            [gimel.config :refer [load-config get-config get-webroot]])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-c" "--config CONFIGPATH" "Path to configfile"
    :default (str (System/getenv "HOME") "/.config/gimel/gimel.edn")
    :id :configfile]
   ["-h" "--help"]])

(defonce server (atom nil))

(defn -main
  [& args]
  (let [options (parse-opts args cli-options)]
    (load-config (:configfile (:options options)))
    (mount/start)
    (let [config (get-config)
          webroot (get-webroot)
          api-handler (create-api-handler)
          static-handler (wrap-file (wrap-defaults (fn [_] {:status 404}) site-defaults) webroot)
          app (fn [request]
                (let [response (static-handler request)]
                  (if (= 404 (:status response))
                    (api-handler request)
                    response)))]
      (reset! server (jetty/run-jetty
                      app
                      {:port (or (:port (:options options)) (:port (:public (:configuration config))) 3000)
                       :send-server-version? false
                       :join? false})))))
