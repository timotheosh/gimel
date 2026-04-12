(ns gimel.api.core
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [reitit.ring :as ring]
            [gimel.static-pages :refer [export]]))

(s/def :gimel/check-directory-path
  #(and (string? %) (not (str/blank? %)) (.isDirectory (io/file %))))

(defn valid-data? [data]
  (and (s/valid? :gimel/check-directory-path (:source data))
       (s/valid? :gimel/check-directory-path (:public data))
       (or (nil? (:org-path data))
           (s/valid? :gimel/check-directory-path (:org-path data)))))

(defn json-response [status body]
  {:status  status
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str body)})

(defn handle-export [_request]
  (export)
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "ok"})

(defn handle-export-custom [request]
  (let [body (try (json/read-str (slurp (:body request)) :key-fn keyword)
                  (catch Exception _ nil))]
    (if (and body (valid-data? body))
      (do (export (:source body) (:public body) (:org-source body))
          (json-response 200 {:status "ok"}))
      (json-response 400 {:error "Invalid input data"}))))

(defn not-found-handler [_]
  (json-response 404 {:error "API endpoint not found"}))

(defn create-api-handler []
  (ring/ring-handler
   (ring/router
    [["/api/export"        {:get handle-export}]
     ["/api/export-custom" {:post handle-export-custom}]])
   (ring/create-default-handler {:not-found not-found-handler})))
