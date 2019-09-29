(ns gimel.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [optimus.prime :as optimus]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets-autorefresh
                                        serve-frozen-assets]]
            [gimel.config :as config]
            [gimel.web :as web]
            [gimel.templates :as tmpl]))


(def dev-app
  (-> web/handler
      (optimus/wrap
       tmpl/get-assets
       optimizations/none
       serve-live-assets-autorefresh)
      (wrap-defaults
       (assoc site-defaults :static false))))
