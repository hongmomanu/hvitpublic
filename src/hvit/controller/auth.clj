(ns hvit.controller.auth
  (:use compojure.core)

  (:require [hvit.views.layout :as layout]
            [hvit.models.db :as db]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]

            )
  )




(defn valid? [id pass pass1]
  (vali/rule (vali/has-value? id)
    [:id "user ID is required"])
  (vali/rule (not (db/get-user id))
    [:id "duplicated user ID"])
  (vali/rule (vali/min-length? pass 1)
    [:pass "密码必须至少1个字符"])
  (vali/rule (= pass pass1)
    [:pass1 "entered passwords do not match"])
  (not (vali/errors? :id :pass :pass1)))




(defn register [& [id]]
  (layout/render
    "registration.html"
    {:id id
     :id-error (vali/on-error :id first)
     :pass-error (vali/on-error :pass first)
     :pass1-error (vali/on-error :pass1 first)}))

(defn handle-registration [username password pass1]
  (if (valid? username password pass1)
    (try
      (do
        (db/create-user {:username username :password (crypt/encrypt password)})
        (session/put! :user-id username)
        (resp/redirect "/"))
      (catch Exception ex
        (vali/rule false [:id (.getMessage ex)])
        (register)))
    (register username)))

(defn addnewuser [username displayname  password divisionid roleid]
  (let [user (db/get-user username)]
    (if(> (count user) 0) (resp/json {:success false :msg "用户已存在"})

      )

    (resp/json {:success true :msg (db/create-user {:username username :password (crypt/encrypt password)
                                                    :displayname displayname :divisionid divisionid
                                                    :roleid roleid
                                                    })})
    )
  )

(defn profile []
  (layout/render
    "profile.html"
    {:user (db/get-user (session/get :user-id))}))

(defn update-profile [{:keys [first-name last-name email]}]
  (db/update-user (session/get :user-id) first-name last-name email)
  (profile))

(defn handle-login [username password]
  (let [user (db/get-user username)]
    (if (and user (crypt/compare password (:password user)))
      (do (session/put! :user-id username) (session/put! :userid (:id user)) (session/put! :roleid (:roleid user)))(session/put! :login-error "用户密码错误"))
    (resp/redirect "/")))

(defn logout []
  (session/clear!)
  (resp/redirect "/"))

(defn getapps []
  (let [
        username (session/get :user-id)
        apps (if (nil? username) [] (db/get-apps (:roleid (db/get-user username))))
        ]
    apps
    )
  )
(defn getusers [start limit  totalname rowsname]
  (let [results (db/getusers start limit)
         nums  (:counts (first (db/getusernums)))
        ]
    (resp/json (assoc {} rowsname results totalname nums))
    )
  )
(defn deluser [userid]
  (resp/json {:success true :msg (db/deluser userid)})
  )
(defn getenums [start limit  totalname rowsname keyword]
  (let [results (db/getenums keyword start limit )
        nums  (:counts (first (db/getenumnums keyword)))
        ]
    (resp/json (assoc {} rowsname results totalname nums))
    )
  )
(defn getroles [start limit  totalname rowsname keyword]
  (let [results (db/getroles keyword start limit )
         nums  (:counts (first (db/getrolenums keyword)))
        ]
    (if(nil? totalname) (resp/json results) (resp/json (assoc {} rowsname results totalname nums)))

    )
  )
(defn getlogs [start limit  totalname rowsname keyword]
  (let [results (db/getlogs keyword start limit )
         nums  (:counts (first (db/getlognums keyword)))
        ]
    (if(nil? totalname) (resp/json results) (resp/json (assoc {} rowsname results totalname nums)))

    )
  )
(defn functreeformat [item funcids]
  (let [
         childnums (count (db/getfuncsbypid (:id item)))
         state (if (> childnums 0) "closed" "open")
         formatitem (assoc item "state" state "textold" (:text item) "checked" (some #(= (:id item) %) funcids))
         ]

    (if (> childnums 0) (conj  formatitem {:text (str (:text formatitem) "(" childnums ")")}) formatitem)
    )

  )

(defn getroleid [roleid]
  (println "session ssssss roleid" (session/get :roleid))
  (println "url roleid" roleid)
  (if (nil? roleid) (session/get :roleid) roleid)

  )

(defn getuserid [userid]
  (if (nil? userid) (session/get :userid) userid)
  )

(defn makerolefunc [roleid deleteid funcid]
  (let[
        delids (read-string deleteid)
        funcids (read-string funcid)
        roleid (getroleid roleid)
        ]
    (dorun (map #(db/delrolefucbyid roleid %) delids))
    (dorun (map #(when (= (count (db/isrolehasfunc roleid %)) 0 ) (db/insertrolefucbyid roleid %)) funcids))
    (resp/json {:success true})

    )


  )

(defn addlog [logcontent userid callback]
  (let [

         userid (getuserid userid)


         ]
    (if (nil? callback) (resp/json {:success true :msg (db/addlog logcontent userid)})(resp/jsonp callback {:success true :msg (db/addlog logcontent userid)}))
    )

  )

(defn gettreefunc [node roleid callback]
  (let [

         results (db/getfuncsbypid node)
         roleid (getroleid roleid)
         funcids (into [](map #(:funcid %) (db/getfuncsbyid roleid)))
         resultsformat (map #(functreeformat % funcids) results)

         ]
    (if (nil? callback) (resp/json resultsformat)(resp/jsonp callback resultsformat))
    )
  )


(defn divisiontreeformat [item]
  (let [
         childnums (count (db/getdivisionsbypid (:id item)))
         state (if (> childnums 0) "closed" "open")
         formatitem (assoc item "state" state "textold" (:text item))
         ]

    (if (> childnums 0) (conj  formatitem {:text (str (:text formatitem) "(" childnums ")")}) formatitem)
    )

  )

(defn adddivision [divisionid divisionname signaturepath divisionpath]
  (if (> (count (db/getdivisionbypath (str divisionpath divisionname))) 0)(resp/json {:success false :msg "行政区划已存在"})
    (let [
           result (db/adddivision {:parentid divisionid :divisionname divisionname :signaturepath signaturepath
                               :divisionpath (str divisionpath divisionname)})
           ]
      (resp/json {:success true :msg result})
      ))

  )
(defn gettreedivision [node callback]

  (let [

         results (db/getdivisionsbypid node)
         resultsformat (map #(divisiontreeformat %) results)

         ]
    (if (nil? callback) (resp/json resultsformat)(resp/jsonp callback resultsformat))
    )

  )


(defn editfunc [funcname label funcid pid imgcss sortnum]
  (let [existfunc (db/getfuncsbytype funcname )
        ]
    (if(and (> (count existfunc) 0) (not= (:id (first existfunc)) (read-string funcid)))
      (resp/json {:success false :msg "功能名已存在"})
      (let [
             result (db/updatefunc {:funcname funcname :label label :pid pid
                                    :imgcss imgcss :sortnum sortnum} funcid)
             ]
        (resp/json {:success true :msg result}))
      )
    )

  )
(defn editenum [enumeratelabel enumeratetype enumeratevaluem id]

          (let [
                 result (db/updateenum {:enumeratelabel enumeratelabel :enumeratetype enumeratetype
                                        :enumeratevalue enumeratevaluem } id)
                 ]
            (resp/json {:success true :msg result}))

      )

(defn delfunc [funcid]
  (resp/json {:success true :msg (db/delfunc funcid)})
  )

(defn deldivision [divisionid]
  (resp/json {:success true :msg (db/deldivision divisionid)})
  )

(defn delenum [id]
  (resp/json {:success true :msg (db/delenum id)})
  )

(defn addfunc [funcname label funcid  imgcss sortnum]
  (if (> (count (db/getfuncsbytype funcname )) 0)(resp/json {:success false :msg "功能名已存在"})
  (let [
         result (db/addfunc {:funcname funcname :label label :pid funcid
                                :imgcss imgcss :sortnum sortnum})
         ]
    (resp/json {:success true :msg result})
    ))
  )

(defn addnewenum [enumlabel enumtype enumvalue]

    (let [
           result (db/addenumerate {:enumeratelabel enumlabel :enumeratetype enumtype  :enumeratevalue enumvalue})
           ]
      (resp/json {:success true :msg result})
      )
  )
(defn addnewrole [rolename]
  (let [
         nums  (:counts (first (db/getrolenums rolename)))
         ]
    (if (> nums 0)(resp/json {:success false :msg "功能名已存在"})
      (resp/json {:success true :msg (db/addrole rolename)})  )
    )


  )
(defn getfuncsbyrole [type roleid callback]
  (let [
         roleid (getroleid roleid)
         funcids (into [](map #(:funcid %) (db/getfuncsbyid roleid)))

         typepid (first (db/getfuncsbytype type))
         funcstype (if (nil? typepid) [] (db/getfuncsbypid (:id typepid)))
         reuslts (filter (fn [x] (some #(= (:id x) %) funcids)) funcstype)
         ]
    (if (nil? callback) (resp/json reuslts)(resp/jsonp callback reuslts))
    )

  )

(defn getenumbytype [type callback]
  (let [

         reuslts (db/getenumeratebytype type)
         ]
    (if (nil? callback) (resp/json reuslts)(resp/jsonp callback reuslts))
    )

  )