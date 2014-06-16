(ns hvit.controller.index


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
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            )
  )


(def index-writer (atom {}))
(def index-searcher (atom {}))


(defn searchindex [text indexname]
  (let [
         analyzer (new MaxWordAnalyzer)
         indexdir (str schema/datapath "index" "/" indexname)
         modtime (fs/mod-time indexdir)
         dir (FSDirectory/open (new File indexdir))
         reader     (DirectoryReader/open dir)
         indexsearcher (get @index-searcher indexname)
         searcher (if (or (nil? indexsearcher)(not= (:time indexsearcher) modtime)) (let [searcher (new IndexSearcher reader)]
                                             (swap! index-searcher assoc indexname
                                               {:searcher searcher :time (fs/mod-time indexdir)})
                                             (println "ssssssssssssssssssssss")
                                             searcher
                                             )(:searcher indexsearcher))

         parser (new QueryParser Version/LUCENE_43 "text" analyzer)
         query (.parse parser text)
         tds (.search searcher query 10000)
         sd (.scoreDocs tds)

         ]
    (println (fs/mod-time indexdir))
    ;(doall (map #(println (.get (.doc searcher (.doc %)) "text")) (to-array sd)))

    (resp/json {:sucess true :result (map #(.get (.doc searcher (.doc %)) "text") (to-array sd))})
    )

  )
(defn addindex [text indexname]

  (let [
         analyzer (new MaxWordAnalyzer)
         config (new IndexWriterConfig Version/LUCENE_43 analyzer)
         configdo (.setOpenMode config IndexWriterConfig$OpenMode/CREATE)
         basicdir  (str schema/datapath "index" "/")
         tabledir (str basicdir indexname "/")
         tabledirdo (when-not (fs/exists? tabledir) (fs/mkdir tabledir))
         dir     (FSDirectory/open (new File tabledir))
         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [iw (new IndexWriter dir config)]
                                                    (swap! index-writer assoc indexname iw)
                                                    iw
                                                    ) indexwriter)
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



