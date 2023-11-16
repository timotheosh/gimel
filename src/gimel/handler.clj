(ns gimel.handler
  (:require [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [gimel.config :as config]))

(def webroot (:webroot (:public (:configuration @(config/read-config)))))

(def dev-app
  (-> (fn [request] {:status 404 :body "Not Found"})
      (wrap-defaults site-defaults)
      (wrap-file webroot)))
