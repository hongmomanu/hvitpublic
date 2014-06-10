(ns hvit.controller.webmap
  (:use compojure.core)

  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            [hvit.views.layout :as layout]
            [clj-http.client :as client]
            [hvit.controller.auth :as auth]
            )
  )


(defn webgis []
  (let [roleid (auth/getroleid nil)]

    (if (nil? roleid) (layout/render "error.html")(layout/render "basemap.html") )
    )

  )
