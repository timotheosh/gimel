(ns gimel.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def config-file (str (System/getenv "HOME") "/.config/gimel/gimel.edn"))

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

(defn read-config
  "Reads config file and returns atom."
  ([] (read-config config-file))
  ([config]
   (if-not (.exists (io/as-file config))
     (atom (read-edn (io/resource "config/gimel.edn")))
     (atom (read-edn (io/file config))))))

(defn write-config
  "Writes config to file."
  ([data] (write-config data config-file))
  ([data config]
   (when-not (.exists (io/as-file config))
     (io/make-parents config))
   (spit config (pr-str @data))))
