(ns gimel.highlight-test
  (:require [clojure.test :refer :all]
            [gimel.highlight :as highlight]
            [net.cgrand.enlive-html :as enlive]
            [hiccup.core :refer [html]]))

(deftest highlight-code-blocks-test
  (testing "highlight-code-blocks function"
    (let [html-input (html [:html [:body [:pre [:code {:class "clojure"} "(+ 1 1)"]]]])
          nodes (enlive/html-resource (java.io.StringReader. html-input))
          transformed-nodes (highlight/highlight-code-blocks nodes)
          result-html (apply str (enlive/emit* transformed-nodes))]
      (is (clojure.string/includes? result-html "class=\"language-clojure line-numbers\"")))))
