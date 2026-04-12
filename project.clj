(defproject gimel "0.1.0-SNAPSHOT"
  :description "Flat file CMS in Clojure"
  :url "https://github.com/timotheosh/gimel"
  :license {:name "MIT License"
            :url "https://www.opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.4"]
                 [toml "0.1.4"]
                 [ring/ring-jetty-adapter "1.15.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/data.json "2.5.2"]
                 [ring/ring-codec "1.3.0"]
                 [ring/ring-defaults "0.7.0"]
                 [metosin/reitit-ring "0.10.1"]
                 [hiccup "2.0.0"]
                 [markdown-clj "1.12.7"]
                 [com.kiranshila/cybermonday "0.6.215" :exclusions [org.jsoup/jsoup]]
                 [enlive "1.1.6"]
                 [optimus "2025.01.19.2" :exclusions [org.clojure/data.json]]
                 [stasis "2023.11.21"]
                 [tick "1.0"]
                 [sitemap "0.4.0"]
                 [mount "0.1.23"]
                 [com.github.seancorfield/next.jdbc "1.3.1093" :exclusions [org.clojure/java.data]]
                 [org.xerial/sqlite-jdbc "3.51.3.0" :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/tools.cli "1.4.256"]
                 [org.clojure/tools.logging "1.3.1"]
                 [ch.qos.logback/logback-classic "1.5.32"]
                 [org.clojure/test.check "1.1.3"]
                 [ring/ring-mock "0.6.2"]]
  :plugins [[lein-shell "0.5.0"]]
  :aliases {"emacs-test" ["shell" "emacs" "--batch"
                          "--eval" "(add-to-list 'load-path \"resources/emacs\")"
                          "--eval" "(add-to-list 'load-path \"resources/emacs/vendor\")"
                          "-l" "ert" "-l" "org"
                          "-l" "resources/emacs/gimel.el"
                          "-l" "resources/emacs/tests/gimel-test.el"
                          "-f" "ert-run-tests-batch-and-exit"]
            "test-all" ["do" "test," "emacs-test"]}
  :main ^:skip-aot gimel.core
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]
                             [lein-cloverage "1.2.2"]]}}
  :test-refresh {:watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]})
