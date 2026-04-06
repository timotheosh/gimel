(ns gimel.static-files-test
  (:require [clojure.test :refer :all]
            [gimel.static-files :as sf]
            [clojure.java.io :as io]))

(deftest checksum-test
  (testing "checksum function"
    (let [temp-file (java.io.File/createTempFile "checksum-test" ".txt")
          file-path (.getAbsolutePath temp-file)]
      (try
        (spit temp-file "hello world")
        (is (= "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9" (sf/checksum file-path)))
        (finally
          (.delete temp-file))))))
