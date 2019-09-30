(defproject gimel "0.1.0-SNAPSHOT"
  :description "Flat file CMS in Clojure"
  :url "https://github.com/timotheosh/gimel"
  :license {:name "MIT License"
            :url "https://www.opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [buddy/buddy-hashers "1.4.0"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [bidi "2.1.6"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.10.0"]
                 [enlive "1.1.6"]
                 [optimus "0.20.2"]
                 [stasis "2.5.0"]
                 [spootnik/watchman "0.3.7"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:port 8880
         :handler gimel.handler/dev-app
         :auto-refresh? true
         :auto-reload? true}
  :main ^:skip-aot gimel.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
