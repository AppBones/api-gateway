(ns appbone-api-gateway.config
  (:require [config.core :refer [env]]
            [mount.core :refer [defstate]]))

(defstate config
  :start env)
