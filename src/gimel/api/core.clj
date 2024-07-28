(ns gimel.api.core
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [reitit.ring :as ring]
            [liberator.core :refer [defresource]]
            [gimel.static-pages :refer [export]]))

(s/def :gimel/check-directory-path #(.isDirectory (io/file %)))

(defn valid-data? [context]
  (let [data (:request context)
        source (s/valid? :gimel/check-directory-path (:source data))
        public (s/valid? :gimel/check-directory-path (:public data))
        org-path (if (:org-path data) (s/valid? :gimel/check-directory-path (:org-path data)) "")]
    (if (and source public org-path)
      (assoc context :export-data data)
      {:status 400
       :body (json/write-str {:error "Invalid input data"})})))

(defn export-site [data]
  (export (:source data) (:public data) (:sitemap-source data))
  {:status 200 :body (json/write-str {:status "ok"})})

(defresource export-site-config
  :allowed-methods [:get]
  :available-media-types ["text/plain"]
  :handle-ok (fn [context]
               (export)
               {:status 200 :body "ok"}))

(defresource export-site-custom
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :malformed? (complement valid-data?)
  :post! (fn [context]
           (let [data (:export-data context)]
             (export-site data))))

(defn wrap-api-404 [handler]
  (fn [request]
    (let [response (handler request)]
      (if (= 404 (:status response))
        ;; Return your custom 404 response for API routes
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"API endpoint not found\"}"}
        response))))

(defn not-found-handler [request]
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body "{\"error\": \"API endpoint not found\"}"})

(defn create-api-handler []
  (let [api-routes [["/api/export" export-site-config]
                    ["/api/export-custom" export-site-custom]]
        router (ring/router api-routes {:data {:middleware [wrap-api-404]}})
        default-handler (ring/create-default-handler {:not-found not-found-handler})]
    (ring/ring-handler router default-handler)))

(def not-found-route
  ["/not-found"
   (fn [_] {:status 404 :body "Not Found"})])

(defn create-handler []
  (ring/ring-handler
   (ring/router
    [["/api/export" export-site-config]
     ["/api/export-custom" export-site-custom]
     not-found-route])))
