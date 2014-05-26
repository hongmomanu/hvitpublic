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

  (POST "/auth/getfuncsbyrole" [type roleid callback]
    (auth/getfuncsbyrole type roleid callback))

  (GET "/auth/getfuncsbyrole" [type roleid callback]
    (auth/getfuncsbyrole type roleid callback))

  (GET "/auth/getenumbytype" [type  callback]
    (auth/getenumbytype type  callback))

  (POST "/auth/getusers" [start limit  totalname rowsname]
    (auth/getusers start limit  totalname rowsname))

  (POST "/auth/gettreefunc" [node roleid callback]
    (auth/gettreefunc node roleid callback))
  (GET "/auth/gettreefunc" [node roleid callback]
    (auth/gettreefunc node roleid callback))



  (POST "/auth/editfunc" [funcname label funcid pid imgcss sortnum]
    (auth/editfunc funcname label funcid pid imgcss sortnum))

  (POST "/auth/editenum" [enumeratelabel enumeratetype enumeratevalue id]
    (auth/editenum enumeratelabel enumeratetype enumeratevalue id))

  (POST "/auth/addnewfunc" [funcname label funcid  imgcss sortnum]
    (auth/addfunc funcname label funcid  imgcss sortnum))

  (POST "/auth/delfunc" [funcid]
    (auth/delfunc funcid))

  (POST "/auth/makerolefunc" [roleid deleteid funcid]
    (auth/makerolefunc roleid deleteid funcid))


  (POST "/auth/getroles" [start limit totalname rowsname keyword]
    (auth/getroles start limit  totalname rowsname keyword))

  (POST "/auth/delenum" [id]
    (auth/delenum id))


  (POST "/auth/addnewenum" [enumlabel enumtype enumvalue]
    (auth/addnewenum enumlabel enumtype enumvalue))

  (POST "/auth/getenums" [start limit totalname rowsname keyword]
    (auth/getenums start limit totalname rowsname keyword))



  (GET "/logout" []
        (auth/logout)))
