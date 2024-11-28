(ns gimel.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
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

(defn load-config
  "Loads the config data into our global atom."
  [config]
  (let [config-path (expand-home config)]
    (if-not (.exists (io/as-file config-path))
      (reset! config-data (check-config (read-edn (io/resource "config/gimel.edn"))))
      (reset! config-data (check-config (read-edn (io/file config-path)))))))

(defn get-config
  "Returns the configuration data."
  []
  (if (zero? (count @config-data))
    (throw (ex-info "Config is blank!!!" {:what-happened? "who knows?"}))
    @config-data))

(defn get-sitemap-source
  "Returns the source directory for generating the sitemap."
  []
  (:sitemap-source (:public (:configuration (get-config)))))

(defn get-source-dir
  "Returns the source directory."
  []
  (:source-dir (:public (:configuration (get-config)))))

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
