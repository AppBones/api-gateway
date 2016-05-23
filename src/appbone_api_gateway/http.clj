(ns appbone-api-gateway.http
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [mount.core :refer [defstate]]
            [compojure.core :refer [routes context defroutes ANY]]
            [appbone-api-gateway.middleware :refer :all]
            [appbone-api-gateway.config :refer [config]]))

(def api-routes
  (routes
   (context "/api" []
     (context "/:version{v[0-9]+}" [version]
       (context "/:service" [service]
         (wrap-match-service
          (routes
           (ANY "/*" [] api-request-handler)))))
     (context "/:service" [service]
       (wrap-match-service
        (routes
         (ANY "/*" [] api-request-handler)))))))

(def gateway
  (wrap-params
   (routes
    api-routes
    (ANY "/oauth2/*" [] api-request-handler))))

(defstate http
  :start (let [port (read-string (:port config))]
           (println "Starting HTTP component on port" port "...")
           (run-server gateway {:join? false :port port}))
  :stop (http))
