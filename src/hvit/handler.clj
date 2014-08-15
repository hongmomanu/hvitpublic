(ns hvit.handler
  (:require [compojure.core :refer [defroutes]]
            [hvit.routes.home :refer [home-routes]]
            [hvit.middleware :as middleware]
            [noir.util.middleware :refer [app-handler]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [hvit.routes.auth :refer [auth-routes]]
            [hvit.routes.test :refer [test-routes]]
            [hvit.routes.index :refer [index-routes]]
            [hvit.routes.webmap :refer [webmap-routes]]
            [hvit.models.schema :as schema]))

(defroutes
  app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info,
     :enabled? true,
     :async? false,
     :max-message-per-msecs nil,
     :fn rotor/appender-fn})
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "hvit.log", :max-size (* 512 1024), :backlog 10})
  (if (env :dev) (parser/cache-off!))
  (if-not (schema/initialized?) (schema/create-tables))
  (timbre/info "hvit started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "hvit is shutting down..."))

(def app
 (app-handler
   [auth-routes home-routes test-routes webmap-routes index-routes app-routes]
   :middleware
   [middleware/template-error-page middleware/log-request ]
   :access-rules
   []
   :formats
   [:json-kw :edn]))

