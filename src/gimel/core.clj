(ns gimel.core
  (:require [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.adapter.jetty :as jetty]
            [gimel.config :as config]
            [gimel.web :refer [start-watcher handler]])
  (:gen-class))

(def webroot (:webroot (:public (:configuration @(config/read-config)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (defn app
    []
    (-> (handler)
        (wrap-defaults site-defaults)
        (wrap-file webroot)))

  (start-watcher)
  (defonce server (jetty/run-jetty
                   app
                   {:port 8880
                    :send-server-version? false
                    :join? false})))
