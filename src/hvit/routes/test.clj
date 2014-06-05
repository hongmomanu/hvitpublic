(ns hvit.routes.test
  (:use compojure.core)
  (:require [hvit.views.layout :as layout]
            [hvit.util :as util]
            [hvit.controller.sqltest :as sqltest]
            [clj-http.client :as client]
            ))



(defroutes test-routes
  (GET "/test" [] (sqltest/sql))
  (GET "/sessiontest" req (sqltest/sessiontest req))
  )
