(ns hvit.routes.home
  (:use compojure.core)
  (:use selmer.filters)
  (:require [hvit.views.layout :as layout]
            [hvit.controller.auth :as auth]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as session]
            [hvit.util :as util]))

(add-filter! :rowfirstspan #(if (= (rem % 3) 0) "margin-left: 0;" ""))
(defn home-page []
  (let [
    content (auth/getapps)
    size (count  (:apps content))
    sessionuid   (:sessionuid content)
         ]
    (layout/render
      "home.html" {:content {:content (:apps content) :nums size :sessionuid sessionuid}
                   ;:login-error  (session/get :login-error)
                   }))
  )

(defn login-page []

  (layout/render
    "login.html" )
  )
  ;(util/md->html "/md/docs.md")

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page ))
  (GET "/loginpage" [] (login-page ))
  (GET "/main" [roleid] (resp/redirect (str "/html/main.html?roleid=" roleid)))
  (GET "/about" [] (about-page)))
