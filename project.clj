(defproject gimel "0.1.0-SNAPSHOT"
  :description "Flat file CMS in Clojure"
  :url "https://github.com/timotheosh/gimel"
  :license {:name "MIT License"
            :url "https://www.opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [buddy/buddy-hashers "2.0.167"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.4.0"]
                 [bidi "2.1.6"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.11.7"]
                 [org-parser "0.1.27"]
                 [enlive "1.1.6"]
                 [optimus "2023-10-03"]
                 [stasis "2023.06.03"]
                 [juxt/dirwatch "0.2.5"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:port 8880
         :handler gimel.handler/dev-app
         :init gimel.web/start-watcher
         :auto-refresh? true
         :auto-reload? true}
  :main ^:skip-aot gimel.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
