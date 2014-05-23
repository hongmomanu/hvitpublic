(ns hvit.routes.test
  (:use compojure.core)
  (:require [hvit.views.layout :as layout]
            [hvit.util :as util]
            [hvit.controller.sqltest :as sqltest]
            ))



(defroutes test-routes
  (GET "/test" [] (sqltest/test))

  )
