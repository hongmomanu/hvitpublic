(ns hvit.routes.webmap
  (:use compojure.core)
  (:require [hvit.views.layout :as layout]
            [hvit.util :as util]

            [clj-http.client :as client]
            [hvit.controller.webmap :as webmap]
            ))



(defroutes webmap-routes
  (GET "/map/webmap" [] (webmap/webgis))
  )
