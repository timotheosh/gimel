(ns gimel.os-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [gimel.os :refer [path-append dirname basename]]))

;;; Unit tests — path-append

(deftest test-path-append-normal-join
  (testing "joins two segments with a single slash"
    (is (= "/foo/bar" (path-append "/foo" "bar"))))
  (testing "joins three segments"
    (is (= "a/b/c" (path-append "a" "b" "c")))))

(deftest test-path-append-consecutive-slash-collapse
  (testing "collapses consecutive slashes from input segments"
    (is (= "a/b" (path-append "a/" "/b"))))
  (testing "collapses multiple consecutive slashes"
    (is (= "a/b/c" (path-append "a//" "b//c")))))

(deftest test-path-append-nil-args
  (testing "skips nil arguments"
    (is (= "a/b" (path-append "a" nil "b"))))
  (testing "all nil returns empty string"
    (is (= "" (path-append nil nil)))))

(deftest test-path-append-empty-string-args
  (testing "skips empty string arguments"
    (is (= "a/b" (path-append "a" "" "b"))))
  (testing "single non-empty segment with empty strings around it"
    (is (= "foo" (path-append "" "foo" "")))))

;;; Unit tests — dirname

(deftest test-dirname-normal
  (testing "returns directory portion of a path"
    (is (= "/foo" (dirname "/foo/bar"))))
  (testing "nested path"
    (is (= "/a/b" (dirname "/a/b/c")))))

(deftest test-dirname-no-slash
  (testing "returns '.' when path has no slash"
    (is (= "." (dirname "filename")))))

(deftest test-dirname-empty-string
  (testing "returns '.' for empty string"
    (is (= "." (dirname "")))))

;;; Unit tests — basename

(deftest test-basename-normal
  (testing "returns filename portion of a path"
    (is (= "bar" (basename "/foo/bar"))))
  (testing "nested path"
    (is (= "c" (basename "/a/b/c")))))

(deftest test-basename-no-slash
  (testing "returns the path itself when no slash present"
    (is (= "filename" (basename "filename")))))

(deftest test-basename-empty-string
  (testing "returns empty string for empty string input"
    (is (= "" (basename "")))))

;;; Property test — path-append produces no consecutive slashes
;;; Validates: Requirements 1.5

(defspec prop-path-append-no-consecutive-slashes 100
  (prop/for-all [segments (gen/vector gen/string-alphanumeric)]
    (not (re-find #"//" (apply path-append segments)))))
