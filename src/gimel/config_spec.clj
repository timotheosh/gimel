(ns gimel.config-spec
  (:require [clojure.spec.alpha :as s]))

;; Specs for individual unqualified keys
(s/def :gimel/sitemap-source string?)
(s/def :gimel/source-dir string?)
(s/def :gimel/webroot string?)
(s/def :gimel/template string?)
(s/def :gimel/footer string?)
(s/def :gimel/web-url #(re-matches #"(http|https)://[^\s]+" %)) ;; Simple URL validation
(s/def :gimel/port pos-int?) ;; Positive integers for ports
(s/def :gimel/dbname string?)

;; Spec for the :public configuration
(s/def :gimel/public
  (s/keys :req-un [:gimel/sitemap-source
                   :gimel/source-dir
                   :gimel/webroot
                   :gimel/template
                   :gimel/footer
                   :gimel/web-url
                   :gimel/port]))

;; Spec for the :database configuration
(s/def :gimel/database
  (s/keys :req-un [:gimel/dbname]))

;; Spec for the entire configuration map
(s/def :gimel/configuration
  (s/keys :req-un [:gimel/public
                   :gimel/database]))

;; Spec for the top-level map
(s/def :gimel/config
  (s/keys :req-un [:gimel/configuration]))


(defn check-config [config-data]
  (if (s/valid? :gimel/config config-data)
    config-data
    (let [error-msg (with-out-str (s/explain :gimel/config config-data))]
      (binding [*out* *err*]
        (println error-msg)
        (throw (ex-info "Invalid config file" {:error error-msg}))))))
