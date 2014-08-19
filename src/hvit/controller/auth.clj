(ns hvit.controller.auth
  (:import
      (java.sql Timestamp)
           )
  (:use compojure.core
        )

  (:require [hvit.views.layout :as layout]
            [hvit.models.db :as db]
            [noir.response :as resp]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [noir.io :as io]
            [hvit.models.schema :as schema]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [ring.util.response :refer [file-response]]

            )
  )

(def cross-domain (atom {}))

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


(defn uploadfile [file]
  (let [uploadpath (str schema/datapath "upload/")
        timenow (c/to-long  (l/local-now))
        filename (str timenow (:filename file))
        ]
    (io/upload-file uploadpath  (conj file {:filename filename}))
    (resp/json {:success true :filename (:filename file) :filepath  (str "../files/" filename)})
    )

  )

(defn getuploadfile [filename]
  ;(println (str datapath "upload/" filename))
  (file-response (str schema/datapath "upload/" filename))
  )

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
(defn getsessionid [req]

  (let [sessionid (:value (get (:cookies req) "ring-session"))
        callback (:callback (:params req))
        success (not (nil? sessionid))
        ]
    (if (nil? callback)(resp/json {:success success :sessionid sessionid})
      (resp/jsonp callback {:success success :sessionid sessionid}))
    )

  )
(defn getsessionidjs [req]

  (let [sessionid (:value (get (:cookies req) "ring-session"))
        callback (:callback (:params req))
        success (not (nil? sessionid))
        ]
    (str "var sessionid='" sessionid "';")

    )

  )
(defn sessioncheck []
  (resp/edn (session/get :user-id))
  )
(defn profile [erromsg]
  (layout/render
    "profile.html"
    {:user (db/get-user (session/get :user-id)) :msg erromsg}))

(defn update-profile [{:keys [passwordold passwordnew]}]
  (let [user (db/get-user (session/get :user-id))
        ispassword (crypt/compare passwordold (:password user))
        ]
    (if ispassword (do (db/updateuser
    {:password (crypt/encrypt passwordnew)} (:id user)) (resp/redirect "/")) (profile "旧密码错误") )

    )
    )


(defn edituser [username displayname password userid]
  (let [user (db/get-user username)
        password (if(> (count password) 30)password (crypt/encrypt password))
        ]
    (if (and (not (nil? user)) (not= (:id  user) (read-string userid)))
      (resp/json {:success false :msg "用户名已存在"})
      (resp/json {:success true :msg (db/updateuser
                                       {:username username :displayname displayname :password password} userid)}))
    )
  )
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
        useritem (db/get-user username)
        apps (if (nil? username) [] (db/get-apps (:roleid useritem)))
        ]
    {:apps apps :sessionuid (:id useritem)}
    )
  )
(defn getusers [start limit  totalname rowsname keyword]
  (let [results (db/getusers start limit keyword )
         nums  (:counts (first (db/getusernums keyword)))
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
(defn getenumskey [keyword callback]
  (let [

         reuslts (db/getenumskey keyword)
         ]
    (if (nil? callback) (resp/json reuslts)(resp/jsonp callback reuslts))
    )
  )
(defn getroles [start limit  totalname rowsname keyword]
  (let [results (db/getroles keyword start limit )
         nums  (:counts (first (db/getrolenums keyword)))
        ]
    (if(nil? totalname) (resp/json results) (resp/json (assoc {} rowsname results totalname nums)))

    )
  )
(defn getlogs [start limit  totalname rowsname keyword bgtime edtime]
  (let [
        bgtime (if (or (nil? bgtime) (= "" (clojure.string/trim bgtime))) "1970-01-01 00:00:00" bgtime)
        edtime (if (or (nil? edtime) (= "" (clojure.string/trim edtime))) "3000-01-01 00:00:00" edtime)
        results (db/getlogs keyword start limit bgtime edtime )
         nums  (:counts (first (db/getlognums keyword bgtime edtime)))
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


(defn authcrossdomainget [req]

  (let [{:keys [params uri content-length content-type cookies headers]} req
        url  (last (clojure.string/split uri #"authcrossdomain"))
        ;geturl (str (:url (session/get :crossdomainlogin)) url)

        ;viewurl (:url (get @cross-domain (last (clojure.string/split (get headers "referer") #"proxylogin\?"))))
        ;templearr (clojure.string/split viewurl #"/")
        ;basurl (if (> (count (clojure.string/split viewurl #"/")) 3) (if (> (.indexOf (last templearr) ".") 0)
        ;                                                               (clojure.string/join "/" (take (- (count templearr) 1) templearr)) viewurl)viewurl)
        baseurl (:baseurl (session/get :crossdomainlogin) )
        geturl (str baseurl url)

        h {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}
        test (println baseurl url geturl "wwwwwwwwwwwwwwwwwwwwww")
        content (client/get geturl {:query-params params   :socket-timeout 10000
                                    :length content-length :content-type  content-type
                                           :conn-timeout 10000
                                           :headers h
                                           :as :stream
                                           :cookie-store (:cs (session/get :crossdomainlogin))
                                           })
        ]
    (:body content)
    )
  )
(defn authcrossdomainpost [req]

  (let [{:keys [form-params params query-string  body content-length content-type uri cookies headers]} req
        url  (last (clojure.string/split uri #"authcrossdomain"))
        ;posturl (str (:url (session/get :crossdomainlogin)) url)
        h {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}
        ;viewurl (:url (get @cross-domain (last (clojure.string/split (get headers "referer") #"proxylogin\?"))))
        ;templearr (clojure.string/split viewurl #"/")
        ;basurl (if (> (count (clojure.string/split viewurl #"/")) 3) (if (> (.indexOf (last templearr) ".") 0)
        ;                                (clojure.string/join "/" (take (- (count templearr) 1) templearr)) viewurl)viewurl)
        baseurl (:baseurl (session/get :crossdomainlogin) )

        posturl (str baseurl url "?" query-string)
        test (println posturl "WWWWWWWWWWWWWWWWWWWWWWWWWWWSSSSSSSSSSSSSSSS")
        content (client/post posturl {
                                       :length content-length :content-type  content-type
                                       :socket-timeout 10000
                                       :conn-timeout 10000

                                       :form-params form-params
                                       :headers h
                                       ;:debug true :debug-body true
                                       :as :auto
                                       :cookie-store (:cs (session/get :crossdomainlogin))
                                       })       ;:form-params (dissoc query-params "url")
        ]

    ;(println (:body content))
    (:body content)
    )
  )


(defn proxylogin [loginurl viewurl loginparams method proxykey  baseurl query-string]


  (let [my-cs (clj-http.cookies/cookie-store)
        ;h {"User-Agent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36" "Cookie" "ASP.NET_SessionId=cvfgvkay4gokjiypy5kraql1"}
        uuid (str (java.util.UUID/randomUUID))
        ]
    ;(println loginurl  loginparams method )

    (try
      (if (= method "get")(client/get loginurl {:query-params (json/read-str loginparams )
                                                 :socket-timeout 5000
                                                 ;;:trace-redirects ["http://192.168.2.112:3000/"]
                                                 ;;:force-redirects true
                                                 ;:headers h
                                                 :follow-redirects false
                                                 :as :auto
                                                 ;:body-encoding "UTF-8"
                                                 :conn-timeout 5000
                                                 :cookie-store my-cs})
        (client/post loginurl {:form-params (json/read-str loginparams)
                               :socket-timeout 50000
                               ;:save-request? true
                               ;:trace-redirects ["http://192.168.2.142:8080/jz"]
                               ;:force-redirects true
                               ;:proxy-host "192.168.2.142"
                               ;:proxy-port 8000
                               ;:headers h

                               ;:debug true :debug-body true

                               ;:body-encoding "UTF-8"
                               :as :auto
                               :conn-timeout 50000
                               :cookie-store my-cs})



        )



      (client/get viewurl { :debug true  :debug-body true :as :auto :cookie-store my-cs })



      (catch Exception e (resp/json {:success false :msg (.getMessage e)}))
      )

    )

  )

(defn simpleproxy [loginurl method loginparams]

  (let [
         query-params (json/read-str loginparams)
          ;test (println  (keys query-params) )
         params (map #(conj {} {:name  % :value (get query-params %)}) (keys query-params))

         ]
    ;(println  params )
      (layout/render "proxyauto.html"
        {:content {:params params :method method :loginurl loginurl};:login-error  (session/get :login-error)
         })
    )
  )


(defn getproxy [req]
  (let [{:keys [params]} req
        content (client/get (:url params) {:query-params (dissoc params :url)  :socket-timeout 10000
                                         :conn-timeout 10000})
        ]
     (:body content)
    )
  )
(defn postproxy [req]
  ;(println (:body req))
  (let [{:keys [query-params body content-length content-type ]} req
        content (client/post (get query-params "url") {:body body :length content-length :content-type  content-type   :socket-timeout 10000
                                         :conn-timeout 10000})       ;:form-params (dissoc query-params "url")
        ]
     (:body content)
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

(defn savedivision [divisionname signaturepath divisionid parentid divisionpath]
  (if (> (count (db/getdivisionbypath (str divisionpath divisionname))) 0)(resp/json {:success false :msg "行政区划已存在"})
    (let [
           result (db/savedivision {:divisionname divisionname :signaturepath signaturepath
                                   :divisionpath divisionpath} divisionid)
           ]
      (resp/json {:success true :msg result})
      ))

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

(defn delrole [roleid]
  (resp/json {:success true :msg (db/delrole roleid)})
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
       reuslts  
    
    )

  )
(defn getfuncsbyroletouter [type roleid callback]

  (let [
    types (clojure.string/split type #",")
    reuslts (if (<= (count types) 1)(getfuncsbyrole type roleid callback)
           (apply conj  (map #(conj {} {(keyword %) (getfuncsbyrole % roleid callback)}) types))
        )
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