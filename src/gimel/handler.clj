(ns gimel.handler
  (:require [ring.middleware.file :refer [wrap-file]]
            [gimel.config :refer [get-webroot]]))

(defn wrap-html-content-type [handler]
  (fn [request]
    (let [response (handler request)]
      (if (.endsWith (:uri request) ".html")
        (assoc-in response [:headers "Content-Type"] "text/html")
        response))))

(def dev-app
  (-> (fn [request] {:status 404 :body "Not Found"})
      (wrap-file (get-webroot))))
