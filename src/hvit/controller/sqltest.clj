(ns hvit.controller.sqltest
  (:use compojure.core)

  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            )
  )




(defn test []
          (resp/json (db/mysql-test))
          ;(resp/json (db/sqlserver-test))
  )
