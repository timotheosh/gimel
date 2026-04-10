(ns gimel.highlight-test
  (:require [clojure.test :refer [deftest testing is]]
            [gimel.highlight :refer [highlight-code-blocks]]))

(deftest test-highlight-code-blocks-transforms-class
  (testing "highlight-code-blocks transforms <pre><code class=\"clojure\"> to class=\"language-clojure line-numbers\""
    (let [html "<pre><code class=\"clojure\">(defn foo [] :bar)</code></pre>"
          result (highlight-code-blocks html)]
      (is (re-find #"class=\"language-clojure line-numbers\"" result)))))

(deftest test-highlight-code-blocks-no-pre-code
  (testing "HTML with no <pre><code> blocks is returned unchanged"
    (let [html "<p>Hello <strong>world</strong></p>"
          result (highlight-code-blocks html)]
      (is (= html result)))))
