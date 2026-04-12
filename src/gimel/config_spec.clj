(ns gimel.config-spec
  (:require [clojure.spec.alpha :as s]))

;; Specs for individual unqualified keys
(s/def :gimel/sitemap-source (s/and string? seq))
(s/def :gimel/source-dir (s/and string? seq))
(s/def :gimel/webroot (s/and string? seq))
(s/def :gimel/template (s/and string? seq))
(s/def :gimel/footer (s/and string? seq))
(s/def :gimel/web-url #(re-matches #"(http|https)://[^\s]+" %)) ;; Simple URL validation
(s/def :gimel/port pos-int?) ;; Positive integers for ports
(s/def :gimel/dbname (s/and string? seq))

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


(defn- first-problem-key [config-data]
  (let [problems (::s/problems (s/explain-data :gimel/config config-data))]
    (when (seq problems)
      (let [{:keys [path pred]} (first problems)
            key-name (last path)]
        (cond
          (and key-name (= pred 'clojure.core/contains?))
          (str "missing required key: " key-name)
          key-name
          (str "invalid value for key: " key-name)
          :else
          (str "invalid config: " pred))))))

(defn check-config [config-data]
  (if (s/valid? :gimel/config config-data)
    config-data
    (let [problem    (first-problem-key config-data)
          error-msg  (or problem (with-out-str (s/explain :gimel/config config-data)))]
      (throw (ex-info (str "Invalid configuration — " error-msg) {:error error-msg})))))
