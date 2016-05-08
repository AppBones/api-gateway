(defproject appbone-api-gateway "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.7"]
                 [com.taoensso/timbre "4.3.1"]
                 [compojure "1.5.0"]
                 [http-kit "2.1.19"]
                 [liberator "0.14.1"]
                 [mount "0.1.10"]
                 [ring "1.4.0"]
                 [yogthos/config "0.8"]]
  :min-lein-version "2.0.0"
  :profiles {:uberjar {:main appbone-api-gateway.core
                       :resource-paths ["config/prod"]
                       :uberjar-name "api-gateway.jar"
                       :aot :all}
             :dev {:source-paths ["dev"]
                   :resource-paths ["config/dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [ring/ring-mock "0.3.0"]]}})
