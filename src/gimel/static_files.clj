(ns gimel.static-files
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [gimel.os :refer [path-append dirname]])
  (:import [java.security MessageDigest]))

(defn checksum [file-path]
  (with-open [fis (io/input-stream file-path)]
    (let [digest (MessageDigest/getInstance "SHA-256")
          buffer (byte-array 8192)
          read-fn (fn read-loop []
                    (let [n (.read fis buffer)]
                      (when (pos? n)
                        (.update digest buffer 0 n)
                        (recur))))]
      (read-fn)
      (->> (.digest digest)
           (map (fn [b] (format "%02x" b)))
           (apply str)))))

(defn ensure-path [path]
  (when-not (.exists (io/file path))
    (io/make-parents path)))

(defn copy-files [src-dir target-root-dir extensions]
  (doseq [file (file-seq (io/file src-dir))]
    (when (and (.isFile file)
               (some #(string/ends-with? (.getName file) %) extensions))
      (let [relative-path (string/replace (str file) src-dir "")  ;; Remove the src-dir from the file path to get the file path relative to the root
            target-file-path (path-append target-root-dir relative-path)]
        (ensure-path target-file-path)
        (when (or (not (.exists (io/file target-file-path)))
                  (and (.exists (io/file target-file-path))
                       (not= (checksum file) (checksum target-file-path)))))
        (io/copy file (io/file target-file-path))))))
