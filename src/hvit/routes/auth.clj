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
  (POST "/auth/addnewuser" [username displayname  password divisionid roleid]
    (auth/addnewuser username displayname  password divisionid roleid))

  (POST "/auth/gettreefunc" [node roleid callback]
    (auth/gettreefunc node roleid callback))
  (GET "/auth/gettreefunc" [node roleid callback]
    (auth/gettreefunc node roleid callback))

  (GET "/auth/gettreedivision" [node  callback]
    (auth/gettreedivision node  callback))

  (POST "/auth/gettreedivision" [node  callback]
    (auth/gettreedivision node  callback))
  (POST "/auth/adddivision" [divisionid divisionname signaturepath divisionpath]
    (auth/adddivision divisionid divisionname signaturepath divisionpath))
  (POST "/auth/deldivision" [divisionid]
    (auth/deldivision divisionid))



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

  (POST "/auth/getlogs" [start limit totalname rowsname keyword bgtime edtime]
    (auth/getlogs start limit  totalname rowsname keyword bgtime edtime))

  (POST "/auth/delenum" [id]
    (auth/delenum id))
  (POST "/auth/deluser" [id]
    (auth/deluser id))

  (POST "/auth/addnewrole" [rolename]
    (auth/addnewrole rolename))


  (POST "/auth/addnewenum" [enumlabel enumtype enumvalue]
    (auth/addnewenum enumlabel enumtype enumvalue))

  (POST "/auth/getenums" [start limit totalname rowsname keyword]
    (auth/getenums start limit totalname rowsname keyword))

  (GET "/auth/addlog" [logcontent userid callback]
    (auth/addlog logcontent userid callback))

  (POST "/auth/uploadfile"[file]

    (auth/uploadfile file)
    )
  (GET "/auth/files/:filename" [filename]
    (auth/getuploadfile filename)
    )

(GET "/logout" []
        (auth/logout)))
