(ns gimel.highlight-test
  (:require [clojure.test :refer :all]
            [gimel.highlight :as highlight]
            [net.cgrand.enlive-html :as enlive]
            [hiccup.core :refer [html]]))

(deftest highlight-code-blocks-test
  (testing "highlight-code-blocks function"
    (let [html-input (html [:html [:body [:pre [:code {:class "clojure"} "(+ 1 1)"]]]])
          result-html (highlight/highlight-code-blocks html-input)]
      (is (clojure.string/includes? result-html "class=\"language-clojure line-numbers\"")))))
