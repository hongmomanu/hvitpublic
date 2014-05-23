(ns hvit.routes.auth
  (:use compojure.core)
  (:require
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [hvit.models.db :as db]
            [hvit.controller.auth :as auth]
            ))



(defroutes auth-routes
  (GET "/register" []
       (auth/register))

  (POST "/register" [username password pass1]
        (auth/handle-registration username password pass1))

  (GET "/profile" [] (auth/profile))
  
  (POST "/update-profile" {params :params} (auth/update-profile params))
  
  (POST "/login" [username password]
        (auth/handle-login username password))

  (POST "/auth/getfuncsbyrole" [type roleid]
    (auth/getfuncsbyrole type roleid))


  (POST "/auth/getusers" [start limit  totalname rowsname]
    (auth/getusers start limit  totalname rowsname))

  (POST "/auth/gettreefunc" [node roleid]
    (auth/gettreefunc node roleid))

  (POST "/auth/editfunc" [funcname label funcid pid imgcss sortnum]
    (auth/editfunc funcname label funcid pid imgcss sortnum))

  (POST "/auth/addnewfunc" [funcname label funcid  imgcss sortnum]
    (auth/addfunc funcname label funcid  imgcss sortnum))

  (POST "/auth/makerolefunc" [roleid deleteid funcid]
    (auth/makerolefunc roleid deleteid funcid))


  (POST "/auth/getroles" [start limit totalname rowsname keyword]
    (auth/getroles start limit  totalname rowsname keyword))



  (GET "/logout" []
        (auth/logout)))
