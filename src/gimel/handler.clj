(ns gimel.handler
  (:require [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [gimel.config :as config]
            [gimel.web :refer [handler]]))

(def webroot (:webroot (:public (:configuration @(config/read-config)))))

(def dev-app
  (-> (handler)
      (wrap-defaults site-defaults)
      (wrap-file webroot)))
