(ns gimel.static-pages-test
  (:require [clojure.test :refer :all]
            [cybermonday.ir :as ir]
            [gimel.static-pages :refer :all]))

(deftest test-flexmark-headers
  (testing "Testing flexmark-headers"
    (is (= (flexmark-headers (nth (ir/md-to-ir "# Section 1") 2))
           [:div
            {:id "section-1", :class "anchor", :href "#section-1"}
            [:h1 {:id "section-1"} "Section 1"]]))
    (is (= (flexmark-headers (nth (ir/md-to-ir "### Subsection 3") 2))
           [:div
            {:id "subsection-3", :class "anchor", :href "#subsection-3"}
            [:h3 {:id "subsection-3"} "Subsection 3"]]))))

(deftest test-flexmark-filter
  (testing "Testing flexmark-filter markdown links"
    (is (= (flexmark-filter {:href "./Places/a-file.md"})
           {:href "./Places/a-file.html"}))
    (is (= (flexmark-filter {:href "./Characters/index.md#albert-einstein"})
           {:href "./Characters/index.html#albert-einstein"}))
    (is (= (flexmark-filter {:href "https://example.com/some-file.md"})
           {:href "https://example.com/some-file.md"}))
    (is (= (flexmark-filter "![an image](./img/an-image.jgp)")
           [:img {:src "./img/an-image.jgp", :alt "an image", :title nil}]))))

(deftest test-mdclj-convert-md-links
  (testing "Testing mdclj-convert-md-links"
    (is (= (mdclj-convert-md-links "[A Lnk](./Places/a-file.md)" :state)
           ["[A Lnk](./Places/a-file.html)" :state]))
    (is (= (mdclj-convert-md-links "[A Lnk](./Places/a-file.md#section-1)" :state)
           ["[A Lnk](./Places/a-file.html#section-1)" :state]))
    (is (= (mdclj-convert-md-links "[A Lnk](https://example.com/some-file.md)" :state)
           ["[A Lnk](https://example.com/some-file.md)" :state]))))
