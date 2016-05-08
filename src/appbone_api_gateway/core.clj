(ns appbone-api-gateway.core
  (:require [mount.core :as mount]
            [appbone-api-gateway.http])
  (:gen-class))

(defn -main [& args]
    (mount/start))
