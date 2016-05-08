(ns appbone-api-gateway.http
  (:require [org.httpkit.server :refer [run-server]]
            [mount.core :refer [defstate]]
            [compojure.core :refer [routes defroutes ANY]]
            [appbone-api-gateway.config :refer [config]]))

(defroutes app
  (ANY "/" [] "I don't do much yet."))

(defstate http
  :start (let [port (read-string (:port config))]
           (println "Starting HTTP component on port" port "...")
           (run-server app {:join? false :port port}))
  :stop (http))
