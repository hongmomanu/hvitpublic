(ns hvit.controller.sqltest
  (:use compojure.core)

  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            [clj-http.client :as client]
            )
  )




(defn sql []
  ;(println (-> (Thread/currentThread) (.getContextClassLoader)(.getResource "") (.getPath)) )
  (println (db/fields-test))
          (resp/json (db/fields-test))
          ;(resp/json (db/sqlserver-test))
  )

(defn sessiontest [req]
  (println 1111111 req)

  (resp/json (:body (client/get "http://localhost:3000/auth/getfuncsbyrole?type=权限设置&callback=funcname"
               {:cookies (:cookies req)})))
  )
