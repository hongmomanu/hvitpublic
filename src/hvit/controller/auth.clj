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
      (session/put! :user-id username)(session/put! :login-error "用户密码错误"))
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
(defn getroles [start limit  totalname rowsname keyword]
  (let [results (db/getroles keyword start limit )
         nums  (:counts (first (db/getrolenums keyword)))
        ]
    (resp/json (assoc {} rowsname results totalname nums))
    )
  )
(defn functreeformat [item]
  (let [
         childnums (count (db/getfuncsbypid (:id item)))
         state (if (> childnums 0) "closed" "open")
         formatitem (assoc item "state" state "textold" (:text item))
         ]
    (if (> childnums 0) (conj  formatitem {:text (str (:text formatitem) "(" childnums ")")}) formatitem)
    )

  )
(defn gettreefunc [node]
  (let [
         results (db/getfuncsbypid node)
         resultsformat (map #(functreeformat %) results)
         ]
    (resp/json resultsformat)
    )
  )
(defn editfunc [funcname label funcid pid imgcss sortnum]
  (let [
         result (db/updatefunc {:funcname funcname :label label :pid pid
                                :imgcss imgcss :sortnum sortnum} funcid)
         ]
    (resp/json {:success true :msg result})
    )
  )

(defn addfunc [funcname label funcid  imgcss sortnum]
  (let [
         result (db/addfunc {:funcname funcname :label label :pid funcid
                                :imgcss imgcss :sortnum sortnum})
         ]
    (resp/json {:success true :msg result})
    )
  )
(defn getfuncsbyrole [type roleid]
  (let [
         funcids (into [](map #(:funcid %) (db/getfuncsbyid roleid)))

         typepid (first (db/getfuncsbytype type))
         funcstype (if (nil? typepid) [] (db/getfuncsbypid (:id typepid)))
         test1 (println funcids )
         test2 (println funcstype )
         reuslts (filter (fn [x] (some #(= (:id x) %) funcids)) funcstype)
         ]
    (println reuslts)
    (resp/json reuslts)
    )

  )