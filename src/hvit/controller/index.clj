(ns hvit.controller.index


  (:import  (org.apache.lucene.document Field Field$Store)
            (org.apache.lucene.store FSDirectory)
            (org.apache.lucene.document TextField)

            (org.apache.lucene.document Document)
            (org.apache.lucene.search IndexSearcher)
            (org.apache.lucene.queryparser.classic QueryParser)

            (org.apache.lucene.index Term IndexWriterConfig IndexWriterConfig$OpenMode IndexWriter DirectoryReader)

            (org.apache.lucene.util Version)
            ;(org.apache.lucene.search.highlight Highlighter SimpleSpanFragmenter QueryScorer)
            (java.io File)
            (com.chenlb.mmseg4j.analysis ComplexAnalyzer MaxWordAnalyzer)

    )
  (:use compojure.core)



  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            [hvit.models.schema :as schema]
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            )
  )


(def index-writer (atom {}))
(def index-searcher (atom {}))

(defn make-searchindex-results [searcher fileds item]
  (apply merge  (map #(assoc {} % (.get (.doc searcher (.doc item)) % ))  fileds))
  )

(defn searchindex [text indexname]
  (let [
         maxnum 10000
         textmap (json/read-str text)
         reqmap (get  textmap "req")
         queryfields (get reqmap "query")
         resmap (get  textmap "res")
         output (if (nil? (get resmap "output")) (assoc {} "start" 0 "limit" maxnum) (get resmap "output"))
         analyzer (new MaxWordAnalyzer)
         indexdir (str schema/datapath "index" "/" indexname)
         modtime (fs/mod-time indexdir)
         dir (FSDirectory/open (new File indexdir))
         reader     (DirectoryReader/open dir)
         indexsearcher (get @index-searcher indexname)
         searcher (if (or (nil? indexsearcher)(not= (:time indexsearcher) modtime)) (let [searcher (new IndexSearcher reader)]
                                             (swap! index-searcher assoc indexname
                                               {:searcher searcher :time (fs/mod-time indexdir)})
                                             searcher
                                             )(:searcher indexsearcher))

         parser (new QueryParser Version/LUCENE_43 (get queryfields "fieldname") analyzer)
         test (println (get queryfields "fieldvalue"))
         query (.parse parser (get queryfields "fieldvalue"))
         tds (.search searcher query maxnum)
         sd (.scoreDocs tds)

         ]
    (println (fs/mod-time indexdir))

    (resp/json {:sucess true
                :result (map #(make-searchindex-results searcher (get resmap "fields") %)
                          (drop (get output "start") (take (+ (get  output "limit") (get output "start"))(to-array sd))))
                :totalCount (alength sd)
                })
    )

  )

(defn delindex [id indexname]
  (let [
         basicdir  (str schema/datapath "index" "/")
         tabledir (str basicdir indexname "/")
         analyzer (new MaxWordAnalyzer)
         config (new IndexWriterConfig Version/LUCENE_43 analyzer)
         tabledirdo (if (fs/exists? tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/APPEND)
                      (do (fs/mkdir tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/CREATE)))
         dir     (FSDirectory/open (new File tabledir))
         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [iw (new IndexWriter dir config)]
                                       (swap! index-writer assoc indexname iw)
                                       iw
                                       ) indexwriter)
         ]
    (.deleteDocuments iw (new Term "id" id))
    (.commit iw )
    (resp/json {:success true})
    )

  )
(defn addindex [text indexname]

  (let [
         textmap (json/read-str text)
         analyzer (new MaxWordAnalyzer)

         basicdir  (str schema/datapath "index" "/")
         tabledir (str basicdir indexname "/")
         config (new IndexWriterConfig Version/LUCENE_43 analyzer)
         tabledirdo (if (fs/exists? tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/APPEND)
                      (do (fs/mkdir tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/CREATE)))
         dir     (FSDirectory/open (new File tabledir))
         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [iw (new IndexWriter dir config)]
                                                    (swap! index-writer assoc indexname iw)
                                                    iw
                                                    ) indexwriter)
         doc  (new Document)
         ]
    (doall (map #(.add doc (new TextField  (first %)  (second %) Field$Store/YES )) textmap))
    (.addDocument iw doc)
    (.commit iw )

    ;(.close iw)
    ;(.optimize iw)

    (resp/json {:sucess true})
    )


  )



