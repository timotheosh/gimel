(defproject gimel "0.1.0-SNAPSHOT"
  :description "Flat file CMS in Clojure"
  :url "https://github.com/timotheosh/gimel"
  :license {:name "MIT License"
            :url "https://www.opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.4.0"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.11.7"]
                 [com.kiranshila/cybermonday "0.6.213"]
                 [enlive "1.1.6"]
                 [optimus "2023.10.19"]
                 [stasis "2023.06.03"]
                 [juxt/dirwatch "0.2.5"]
                 [tick "0.7.5"]
                 [sitemap "0.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.894"]
                 [org.xerial/sqlite-jdbc "3.39.2.1"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:port 8880
         :handler gimel.handler/dev-app
         :init gimel.watcher/start-watcher
         :auto-refresh? true
         :auto-reload? true}
  :main ^:skip-aot gimel.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]
                             [lein-cloverage "1.2.2"]]}}
  :test-refresh {:watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]})
