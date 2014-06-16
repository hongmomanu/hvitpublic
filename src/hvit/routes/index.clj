(ns hvit.routes.index
  (:use compojure.core)
  (:require [hvit.views.layout :as layout]
            [hvit.util :as util]
            [hvit.controller.index :as lindex]
            [clj-http.client :as client]
            ))



(defroutes index-routes

  (GET "/index/addindex" [text indexname] (lindex/addindex text indexname))
  (GET "/index/searchindex" [text indexname] (lindex/searchindex text indexname))
  )
