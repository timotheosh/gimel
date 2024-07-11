(defproject gimel "0.1.0-SNAPSHOT"
  :description "Flat file CMS in Clojure"
  :url "https://github.com/timotheosh/gimel"
  :license {:name "MIT License"
            :url "https://www.opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [ring/ring-jetty-adapter "1.12.2" :exclusions [org.slf4j/slf4j-api]]
                 [ring/ring-defaults "0.5.0"]
                 [metosin/reitit-ring "0.7.1"]
                 [liberator "0.15.3"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.12.1"]
                 [com.kiranshila/cybermonday "0.6.215" :exclusions [org.jsoup/jsoup]]
                 [enlive "1.1.6"]
                 [optimus "2023.11.21" :exclusions [org.clojure/data.json]]
                 [stasis "2023.11.21"]
                 [tick "0.7.5"]
                 [sitemap "0.4.0"]
                 [mount "0.1.18"]
                 [com.github.seancorfield/next.jdbc "1.3.939" :exclusions [org.clojure/java.data]]
                 [org.xerial/sqlite-jdbc "3.46.0.0" :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/tools.cli "1.1.230"]
                 [org.clojure/tools.logging "1.3.0"]
                 [ch.qos.logback/logback-classic "1.5.6"]]
  :main ^:skip-aot gimel.core
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]
                             [lein-cloverage "1.2.2"]]}}
  :test-refresh {:watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]})
