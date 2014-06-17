(ns hvit.controller.sqltest


  (:import  (org.apache.lucene.document Field Field$Store)
            (org.apache.lucene.store FSDirectory)
            (org.apache.lucene.document TextField)

            (org.apache.lucene.document Document)
            (org.apache.lucene.search IndexSearcher)
            (org.apache.lucene.queryparser.classic QueryParser)

            (org.apache.lucene.index IndexWriterConfig IndexWriterConfig$OpenMode IndexWriter DirectoryReader)

            (org.apache.lucene.util Version)
            (java.io File)
            (com.chenlb.mmseg4j.analysis ComplexAnalyzer MaxWordAnalyzer)

    )
  (:use compojure.core)



  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            [hvit.models.schema :as schema]
            [clj-http.client :as client]
            )
  )


(def index-writer (atom {}))


(defn searchindex [text]
  (let [
         analyzer (new MaxWordAnalyzer)
         dir (FSDirectory/open (new File (str schema/datapath "index" "/")))
         reader     (DirectoryReader/open dir)
         searcher ( new IndexSearcher reader)
         parser (new QueryParser Version/LUCENE_43 "text" analyzer)
         query (.parse parser text)
         tds (.search searcher query 10000)
         sd (.scoreDocs tds)

         ]
    ;(println (to-array sd))
    ;(doall (map #(println (.get (.doc searcher (.doc %)) "text")) (to-array sd)))

    (resp/json {:sucess true :result (map #(.get (.doc searcher (.doc %)) "text") (to-array sd))})
    )

  )
(defn addindex [text]

  (let [
         analyzer (new MaxWordAnalyzer)
         config (new IndexWriterConfig Version/LUCENE_43 analyzer)
         configdo (.setOpenMode config IndexWriterConfig$OpenMode/CREATE)
         dir     (FSDirectory/open (new File (str schema/datapath "index" "/")))
         iw    (if (nil? (get @index-writer "iw"))(let [iw (new IndexWriter dir config)]
                                                    (swap! index-writer assoc "iw" iw)
                                                    iw
                                                    ) (get @index-writer "iw"))
         doc  (new Document)
         ]
    (.add doc (new TextField  "text"  text Field$Store/YES ))
    (.addDocument iw doc)
    (.commit iw )
    ;(.close iw)
    ;(.optimize iw)

    (resp/json {:sucess true})
    )


  )



(defn sql []
  ;(println (-> (Thread/currentThread) (.getContextClassLoader)(.getResource "") (.getPath)) )
  ;(println (db/fields-test))
          ;(resp/json (db/fields-test))
  (let [pagelist (db/oraclepage)
        pageids (map #(:id %) pagelist)
        ]
    (println pageids)
    (resp/json {:results (db/oracltest pageids)})
    )

  )



(defn sessiontest [req]
  (println 1111111 req)

  (resp/json (:body (client/get "http://localhost:3000/auth/getfuncsbyrole?type=权限设置&callback=funcname"
               {:cookies (:cookies req)})))
  )
