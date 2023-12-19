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

(def not-found-route
  ["/not-found"
   (fn [_] {:status 404 :body "Not Found"})])

(def router
  (ring/router
   [["/api/export" export-site-config]
    ["/api/export-custom" export-site-custom]
    not-found-route]))

(def handler
  (ring/ring-handler router))
