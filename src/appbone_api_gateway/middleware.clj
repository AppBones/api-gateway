(ns appbone-api-gateway.middleware
  (:require [clojure.walk :refer [stringify-keys]]
            [appbone-api-gateway.util :refer :all]
            [org.httpkit.server :refer [run-server]]
            [mount.core :refer [defstate]]
            [ring.util.response :as resp]
            [org.httpkit.client :as http]))

(defn wrap-jwt-exchange [handler config]
  "Execute the wrapped handler if the provided access_token can be validated and
  exchanged for a JWT on the oauth server. If not valid, return the response
  from the oauth server verbatim. If valid, swap the JWT into the authorization
  header before forwarding the request to the wrapped handler."
  (fn [request]
    (let [qac (get-in request [:query-params "access_token"])
          hac (second (split (get-in request [:headers "Authorization"]) #" "))
          token (or qac hac)
          options {:as :text :query-params {:access_token token}}
          url (str "https://" (:oauth2-provider config) "/oauth2/jwt")
          {:keys [status headers body error]} @(http/get url options)]
      (if error
        (-> (resp/response "Could not reach the authentication server in time.")
            (resp/status 503))
        (if (not= status 200)
          (-> (resp/response body)
              (resp/status status)
              (assoc-in [:headers] (stringify-keys headers)))
          (let [auth (str "Bearer " body)
                req (-> request
                        (assoc-in [:headers "Authorization"] auth)
                        (dissoc-in [:query-params "access_token"])
                        (dissoc-in [:params "access_token"]))]
            (handler req)))))))

(defn wrap-match-service [handler config]
  "Modify incoming requests by changing the destination to the hostname of the
  service that has a name matching the route-parameter, based on the config."
  (fn [request]
    (let [s (get-in request [:route-params :service])
          k (keyword (str "appbone-api-host-" s))
          host (k config)
          req (-> request
                  (assoc :server-name host))]
      (handler req))))

(defn api-request-handler [request]
  "Initiate an HTTP Request on behalf of the sender and forward them the
  response."
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
