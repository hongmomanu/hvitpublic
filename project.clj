(defproject
  hvitpublic
  "0.1.0-SNAPSHOT"
  :repl-options
  {:init-ns hvit.repl}
  :dependencies
  [
   [ring-server "0.3.1"]
   [com.h2database/h2 "1.3.175"]
   [org.xerial/sqlite-jdbc "3.7.15-M1"]
   [com.oracle/ojdbc6 "11.2.0.3"]
   [clj-http "0.9.2"]

   [org.clojure/data.json "0.2.5"]

   [org.apache.lucene/lucene-core "4.3.1"]
   [com.chenlb.mmseg4j/mmseg4j-core "1.10.0"]
   [com.chenlb.mmseg4j/mmseg4j-analysis "1.9.1"]

   ;[mysql/mysql-connector-java "5.1.30"]
   [org.mariadb.jdbc/mariadb-java-client "1.1.7"]
   [net.sourceforge.jtds/jtds "1.2.4"]
   ;[org.wltea.ik-analyzer/ik-analyzer "3.2.8"]
   [hvitmiddleware "0.1.2"]

   [me.raynes/fs "1.4.5"]

   [postgresql/postgresql "9.1-901.jdbc4"]
   [environ "0.5.0"]
   [markdown-clj "0.9.43"]
   [com.taoensso/timbre "3.1.6"]
   [org.clojure/clojure "1.6.0"]
   [com.taoensso/tower "2.0.2"]
   [log4j
    "1.2.17"
    :exclusions
    [javax.mail/mail
     javax.jms/jms
     com.sun.jdmk/jmxtools
     com.sun.jmx/jmxri]]
   [korma "0.3.1"]
   [compojure "1.1.6"]
   [selmer "0.6.6"]
   [lib-noir "0.8.2"]]
  :ring
  {:handler hvit.handler/app,
   :init hvit.handler/init,
   :destroy hvit.handler/destroy}
  :profiles
  {:uberjar {:aot :all},
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}},
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.2"]],
    :env {:dev true}}}
  :url
  "http://example.com/FIXME"
  :plugins
  [[lein-ring "0.8.10"] [lein-environ "0.5.0"] [lein-immutant "1.2.1"]]
  :repositories [
                  ["java.net" "http://download.java.net/maven/2"]
                  ["nexus" "https://code.lds.org/nexus/content/groups/main-repo"]
                  ["sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                               ;; If a repository contains releases only setting
                               ;; :snapshots to false will speed up dependencies.
                               :snapshots false
                               ;; Disable signing releases deployed to this repo.
                               ;; (Not recommended.)
                               :sign-releases false
                               ;; You can also set the policies for how to handle
                               ;; :checksum failures to :fail, :warn, or :ignore.
                               :checksum :fail
                               ;; How often should this repository be checked for
                               ;; snapshot updates? (:daily, :always, or :never)
                               :update :always
                               ;; You can also apply them to releases only:
                               :releases {:checksum :fail :update :always}}]

                  ]
  :description
  "FIXME: write description"
  :jvm-opts ["-Dfile.encoding=utf8"]
  :min-lein-version "2.0.0")