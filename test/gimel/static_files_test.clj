(ns gimel.static-files-test
  (:require [clojure.test :refer [deftest is use-fixtures testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.java.io :as io]
            [gimel.static-files :refer [checksum ensure-path copy-files]])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

;; ---------------------------------------------------------------------------
;; Fixtures
;; ---------------------------------------------------------------------------

(def ^:dynamic *temp-dir* nil)

(defn temp-dir-fixture [f]
  (let [dir (Files/createTempDirectory "gimel-static-test-"
                                       (into-array FileAttribute []))]
    (binding [*temp-dir* (.toFile dir)]
      (try
        (f)
        (finally
          ;; Delete all files in the temp dir, then the dir itself
          (doseq [file (reverse (file-seq *temp-dir*))]
            (.delete file)))))))

(use-fixtures :each temp-dir-fixture)

;; ---------------------------------------------------------------------------
;; Helper
;; ---------------------------------------------------------------------------

(defn make-temp-file
  "Creates a file with the given name and content inside *temp-dir*."
  [name content]
  (let [f (io/file *temp-dir* name)]
    (spit f content)
    f))

;; ---------------------------------------------------------------------------
;; Unit tests — checksum
;; ---------------------------------------------------------------------------

(deftest test-checksum-returns-64-char-hex
  (testing "checksum returns a 64-character lowercase hex string"
    (let [f (make-temp-file "checksum-test.txt" "hello world")
          result (checksum f)]
      (is (= 64 (count result)))
      (is (re-matches #"[0-9a-f]{64}" result)))))

(deftest test-checksum-different-content-differs
  (testing "different file contents produce different checksums"
    (let [f1 (make-temp-file "a.txt" "content-a")
          f2 (make-temp-file "b.txt" "content-b")]
      (is (not= (checksum f1) (checksum f2))))))

;; ---------------------------------------------------------------------------
;; Unit tests — ensure-path
;; ---------------------------------------------------------------------------

(deftest test-ensure-path-creates-parent-dirs
  (testing "ensure-path creates missing parent directories"
    (let [deep-file (io/file *temp-dir* "a" "b" "c" "file.txt")]
      (is (not (.exists (.getParentFile deep-file))))
      (ensure-path (str deep-file))
      (is (.exists (.getParentFile deep-file))))))

(deftest test-ensure-path-existing-path-is-noop
  (testing "ensure-path does not throw when parent already exists"
    (let [f (make-temp-file "existing.txt" "data")]
      (is (nil? (ensure-path (str f)))))))

;; ---------------------------------------------------------------------------
;; Unit tests — copy-files
;; ---------------------------------------------------------------------------

(deftest test-copy-files-copies-matching-extensions
  (testing "copy-files copies only files matching the given extensions"
    (let [src-dir  (io/file *temp-dir* "src")
          dest-dir (io/file *temp-dir* "dest")]
      (.mkdirs src-dir)
      (.mkdirs dest-dir)
      ;; Create source files
      (spit (io/file src-dir "style.css")  "body {}")
      (spit (io/file src-dir "app.js")     "console.log(1)")
      (spit (io/file src-dir "readme.txt") "ignore me")
      ;; Copy only .css and .js
      (copy-files (str src-dir) (str dest-dir) [".css" ".js"])
      (is (.exists (io/file dest-dir "style.css")))
      (is (.exists (io/file dest-dir "app.js")))
      (is (not (.exists (io/file dest-dir "readme.txt")))))))

(deftest test-copy-files-does-not-overwrite-identical
  (testing "copy-files skips files that are already identical"
    (let [src-dir  (io/file *temp-dir* "src2")
          dest-dir (io/file *temp-dir* "dest2")]
      (.mkdirs src-dir)
      (.mkdirs dest-dir)
      (spit (io/file src-dir "data.css") "same content")
      ;; First copy
      (copy-files (str src-dir) (str dest-dir) [".css"])
      (let [dest-file (io/file dest-dir "data.css")
            mtime1    (.lastModified dest-file)]
        ;; Second copy — file should not be overwritten (same checksum)
        (Thread/sleep 50)
        (copy-files (str src-dir) (str dest-dir) [".css"])
        (is (= mtime1 (.lastModified dest-file)))))))

;; ---------------------------------------------------------------------------
;; Property-based test — checksum is idempotent (Property 2)
;; Validates: Requirements 7.2
;; ---------------------------------------------------------------------------

(defspec prop-checksum-idempotent 100
  (prop/for-all [data gen/bytes]
    (let [f (java.io.File/createTempFile "pbt-checksum-" ".bin")]
      (try
        (with-open [out (io/output-stream f)]
          (.write out data))
        (= (checksum f) (checksum f))
        (finally
          (.delete f))))))
