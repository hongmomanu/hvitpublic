(ns hvit.models.db
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [hvit.models.schema :as schema]))

(defdb db schema/db-spec)
(defdb mysqldb schema/db-mysql)

(defdb postgresdb schema/db-postgres)

(defdb sqlserverdb schema/db-sqlserver)

(declare users roles functorole functions enumerate divisions systemlog)
(defentity users
  (pk :roleid)
  (has-one roles {:fk :id})
  (database mysqldb)
  )
(defentity roles
  (has-many functorole {:fk :roleid})
  (database mysqldb)
  )
(defentity functorole
  (pk :funcid)
  (has-one functions {:fk :id})
  (database mysqldb)
  )
(defentity functions

  (database mysqldb)
  )
(defentity enumerate
  (database mysqldb)
 )
(defentity divisions
  (database mysqldb)
 )
(defentity systemlog
  (pk :userid)
  (has-one users {:fk :id})
  (database mysqldb)
 )

(defn create-user [user]
  (insert users
          (values user)))

(defn update-user [id first-name last-name email]
  (update users
  (set-fields {:first_name first-name
               :last_name last-name
               :email email})
  (where {:id id})))

(defn updateuser [fields id]
  (update users
    (set-fields fields)
    (where {:id id})
    )
  )

(defn getusers [start limits]
  (select users

    (fields :username :password :id :roleid :time :displayname)
    (with roles
      (fields :rolename )
      )
    (limit limits)
    (offset start))
  )

(defn deluser [userid]
  (delete users
    (where {:id userid})
    )
  )

(defn getenums [keyword start limits]
  (select enumerate
    (where {:enumeratetype [like (str "%" (if (nil? keyword)"" keyword) "%")]})
    (limit limits)
    (offset start))
  )
(defn getroles [keyword start limits]

  (select roles
    (where {:rolename [like (str "%" (if (nil? keyword)"" keyword) "%")]})
    (limit limits)
    (offset start))

  )
(defn getlogs [keyword start limits bgtime edtime]

  (select systemlog
    (with users
      (fields :username )
      )
    (where (and {:logcontent [like (str "%" (if (nil? keyword)"" keyword) "%")]
            }
             {:time [>= (sqlfn timestamp bgtime)]}
             {:time [<= (sqlfn timestamp edtime)]}
             ))
    (limit limits)
    (offset start))

  )
(defn getenumnums [keyword]

  (select enumerate
    (where {:enumeratetype [like (str "%" (if (nil? keyword)"" keyword) "%")]})
    (aggregate (count :id) :counts)
    )
  )
(defn getrolenums [keyword]

  (select roles
    (where {:rolename [like (str "%" (if (nil? keyword)"" keyword) "%")]})
    (aggregate (count :id) :counts)
    )
  )
(defn addlog [logcontent userid]
  (insert systemlog
    (values {:userid userid :logcontent logcontent})
    )

  )
(defn savedivision [fields divisionid]
  (update divisions
    (set-fields fields)
    (where {:id divisionid})
    )
  )
(defn getlognums [keyword bgtime edtime]

  (select systemlog
    (where (and {:logcontent [like (str "%" (if (nil? keyword)"" keyword) "%")]
                 }
             {:time [>= (sqlfn timestamp bgtime)]}
             {:time [<= (sqlfn timestamp edtime)]}
             ))
    (aggregate (count :id) :counts)
    )
  )
(defn getusernums []
  (select users
    (aggregate (count :id) :counts)
    )
  )
(defn get-user [user]
  (first (select users

           (fields :username :password :id :roleid)
           (with roles
             (fields :rolename )
             )
                 (where {:username user})
                 (limit 1))))

(defn get-apps [roleid]
  (select functorole
    (with functions
      (fields :label :funcname )
      (where {:pid -1} )
      )
    (where {:roleid roleid} )
    )
  )

(defn delrolefucbyid [roleid funcid]
  (delete functorole
    (where {:roleid roleid :funcid funcid} )
    )

  )
(defn deldivision [divisionid]
  (delete divisions
    (where {:id divisionid})
    )
  )
(defn getfuncsbyid [roleid]

  (select functorole
    (with functions
      (fields :label :funcname )
      )
    (where {:roleid roleid} )
    )
  )
(defn isrolehasfunc [roleid funcid]

  (select functorole
    (where {:roleid roleid :funcid funcid} )
    )
  )

(defn insertrolefucbyid [roleid funcid]
  (insert functorole
    (values {:roleid roleid :funcid funcid})
    )
  )

(defn getfuncsbytype [type]

  (select functions
    (fields :id)
    (where {:funcname type})
    )
  )

(defn delfunc [funcid]
  (delete functions
    (where {:id funcid})
    )
  )
(defn delenum [enumid]
  (delete enumerate
    (where {:id enumid})
    )
  )
(defn getfuncsbypid [pid]
  (select functions
    (fields :id [:funcname :text] :pid [:label :value] :imgcss :sortnum)
    (where {:pid pid})
    )
  )

(defn getdivisionsbypid [pid]
  (select divisions
    (fields :id [:divisionname :text] :parentid [:divisionpath :value] :divisionpath :signaturepath)
    (where {:parentid pid})
    )

  )
(defn getdivisionbypath [path]
  (select divisions
    (fields :id)
    (where {:divisionpath path})
    )
  )
(defn updatefunc [fields funcid]
  (update functions
    (set-fields fields)
    (where {:id funcid}))

  )
(defn updateenum [fields enumid]
  (update enumerate
    (set-fields fields)
    (where {:id enumid}))
  )

(defn addfunc [fields]
  (insert functions
    (values fields)
    )
  )
(defn addrole [rolename]
  (insert roles
    (values {:rolename rolename})
    )
  )

(defn addenumerate [fields]
  (insert enumerate
    (values fields)
    )
  )

(defn adddivision [fields]
  (insert divisions
    (values fields)
    )
  )

(defn getenumeratebytype [type]
  (select enumerate
    (where {:enumeratetype type})
    )
  )


(defn postgres-test[]

  (with-db postgresdb
    (exec-raw ["SELECT 1 WHERE 1 = ? " [1]] :results))
  )
(defn mysql-test[]

  (with-db mysqldb
    (exec-raw ["SELECT 2222 " []] :results))
  )
(defn sqlserver-test[]

  (with-db sqlserverdb
    (exec-raw ["SELECT 2 WHERE 1 = ? " [1]] :results))
  )
