(ns appbone-api-gateway.middleware
  (:require [clojure.walk :refer [stringify-keys]]
            [appbone-api-gateway.util :refer :all]
            [org.httpkit.server :refer [run-server]]
            [mount.core :refer [defstate]]
            [ring.util.response :as resp]
            [org.httpkit.client :as http]))

(defn wrap-match-service [handler config]
  (fn [request]
    (let [s (get-in request [:route-params :service])
          k (keyword (str "appbone-api-host-" s))
          host (k config)
          req (-> request
                  (assoc :server-name host))]
      (handler req))))

(defn api-request-handler [request]
  (let [keys [:query-params :headers :form-params :body]
        url (str "https://" (:server-name request) (:uri request))
        options (-> request
                    (select-keys keys)
                    (dissoc-in [:headers "host"])
                    (assoc-in [:method] (:request-method request))
                    (assoc-in [:url] url))
        {:keys [status headers body error opts]} @(http/request options)]
    (if error
      (-> (resp/response "Could not reach the service instance in time.")
          (resp/status 503))
      (-> (resp/response body)
          (resp/status status)
          (assoc-in [:headers] (stringify-keys headers))))))
