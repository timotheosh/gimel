(ns gimel.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [toml.core :as toml]
            [gimel.config-spec :refer [check-config]]))

(defonce config-data (atom {}))

(defn expand-home
  "Expands ~ to the user's home directory in the given path."
  [path]
  (if (.startsWith path "~")
    (str (System/getProperty "user.home") (subs path 1))
    path))

(defn get-file
  "Returns file io object."
  [file-path file]
  (io/file (str file-path "/" file)))

(defn read-edn
  "Reads edn file from io/file"
  [file-data]
  (try
    (with-open [r (io/reader file-data)]
      (edn/read (java.io.PushbackReader. r)))
    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" file-data (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" file-data (.getMessage e)))))

(defn- parse-toml
  "Parses a TOML file and reshapes it into the config map structure
  expected by gimel.config-spec/check-config.
  The [emacs] table is parsed but not stored.
  Throws ex-info with :error key on TOML syntax errors."
  [file-path]
  (let [raw (try
              (toml/read (slurp file-path) :keywordize true)
              (catch Exception e
                (throw (ex-info (str "TOML parse error: " (.getMessage e))
                                {:error (.getMessage e)}))))
        server   (:server raw)
        database (:database raw)]
    {:configuration
     {:public   {:port           (:port server)
                 :web-url        (:web-url server)
                 :webroot        (:webroot server)
                 :snippet-output     (:snippet-output server)
                 :org-source (:org-source server)
                 :template       (:template server)
                 :footer         (:footer server)}
      :database {:dbname (:dbname database)}}}))

(defn load-config
  "Loads the config data into our global atom."
  [config]
  (let [config-path (expand-home config)]
    (if-not (.exists (io/as-file config-path))
      (throw (ex-info (str "Config file does not exist: " config-path)
                      {:file-path config-path
                       :original-path config}))
      (reset! config-data (check-config (parse-toml config-path))))))

(defn get-config
  "Returns the configuration data."
  []
  (if (zero? (count @config-data))
    (throw (ex-info "Config is blank!!!" {:what-happened? "who knows?"}))
    @config-data))

(defn get-port
  "Returns the server port."
  []
  (:port (:public (:configuration (get-config)))))

(defn get-org-source
  "Returns the source directory for generating the sitemap."
  []
  (:org-source (:public (:configuration (get-config)))))

(defn get-snippet-output
  "Returns the source directory."
  []
  (:snippet-output (:public (:configuration (get-config)))))

(defn get-webroot
  "Returns the webroot."
  []
  (:webroot (:public (:configuration (get-config)))))

(defn get-web-url
  "Returns the web url."
  []
  (:web-url (:public (:configuration (get-config)))))

(defn get-template-dir
  "Returns the path to the template for the site."
  []
  (:template (:public (:configuration (get-config)))))

(defn get-footer
  "Returns the footer."
  []
  (:footer (:public (:configuration (get-config)))))

(defn get-dbname
  "Returns the database name."
  []
  (:dbname (:database (:configuration (get-config)))))
