(ns hvit.models.db
  (:use korma.core
        [korma.db :only [defdb with-db]])
  (:require [hvit.models.schema :as schema]))

(defdb db schema/db-spec)
(defdb mysqldb schema/db-mysql)

(defdb postgresdb schema/db-postgres)

(defdb sqlserverdb schema/db-sqlserver)

(declare users roles functorole functions)
(defentity users
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

(defn create-user [user]
  (insert users
          (values user)))

(defn update-user [id first-name last-name email]
  (update users
  (set-fields {:first_name first-name
               :last_name last-name
               :email email})
  (where {:id id})))

(defn getusers [start limits]
  (select users

    (fields :username :password :id :roleid :time)
    (with roles
      (fields :rolename )
      )
    (limit limits)
    (offset start))
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

(defn getfuncsbyid [roleid]

  (select functorole
    (with functions
      (fields :label :funcname )
      )
    (where {:roleid roleid} )
    )
  )
(defn getfuncsbytype [type]

  (select functions
    (fields :id)
    (where {:funcname type})
    )
  )

(defn getfuncsbypid [pid]
  (select functions
    (fields :id [:funcname :text] :pid [:label :value])
    (where {:pid pid})
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
