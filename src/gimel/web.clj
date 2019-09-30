(ns gimel.web
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [bidi.bidi :refer [match-route]]
            [bidi.ring :refer [make-handler]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]
            [optimus.link :as link]
            [ring.util.response :as res]
            [gimel.config :as config]
            [gimel.templates :as tmpl]))

(def admin-conf (:admin (:configuration @(config/read-config))))

(defn response
  "Return the page/data sent wrapped in a ring response."
  [page]
  (res/content-type
   (res/response page)
   "text/html"))

(defn page-layout
  [page]
  (clojure.string/join (tmpl/public-page
                        {:text page
                         :navbar (html [:h1 "HEADAE"])
                         :left-side (html [:h2 "SIDENOTES"])})))

(defn admin-layout
  [page]
  (clojure.string/join (tmpl/admin-page
                        {:text page})))

(defn admin-handler
  [request]
  (response (admin-layout (html [:h1 "Admin Page"]))))

(defn not-found
  [request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (page-layout
          (html [:div
                 [:h1 {:style "color: red; text-align: center"} "404"]
                 [:h1 {:style "color: blue; text-align: center"} "Oopsie"]
                 [:p "Sorry, the page you've requested is not found."]]))})

(def routes
  ["/"
   [["admin" [["" admin-handler]
              ["/" admin-handler]]]
    [true not-found]]])

(def handler
  (make-handler routes))
