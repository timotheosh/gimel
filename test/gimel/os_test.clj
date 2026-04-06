(ns gimel.os-test
  (:require [clojure.test :refer :all]
            [gimel.os :as os]))

(deftest path-append-test
  (testing "path-append function"
    (is (= "a/b/c" (os/path-append "a" "b" "c")))
    (is (= "a/b/c" (os/path-append "a/" "b" "/c")))
    (is (= "a/b/c" (os/path-append "a\\" "b" "\\c")))
    (is (= "a/b/c" (os/path-append "a/b" "c")))
    (is (= "/a/b/c" (os/path-append "/a" "b" "c")))
    (is (= "a/b/" (os/path-append "a" "b/")))))

(deftest dirname-test
  (testing "dirname function"
    (is (= "/home/user" (os/dirname "/home/user/file.txt")))
    (is (= "a/b" (os/dirname "a/b/c")))
    (is (= "a" (os/dirname "a/b")))
    (is (= "." (os/dirname "file.txt")))
    (is (= "/" (os/dirname "/file.txt")))
    (is (= "/" (os/dirname "/")))))

(deftest basename-test
  (testing "basename function"
    (is (= "file.txt" (os/basename "/home/user/file.txt")))
    (is (= "c" (os/basename "a/b/c")))
    (is (= "b" (os/basename "a/b")))
    (is (= "file.txt" (os/basename "file.txt")))
    (is (= "file.txt" (os/basename "/file.txt")))
    (is (= "" (os/basename "/")))
    (is (= "" (os/basename "a/b/")))))
