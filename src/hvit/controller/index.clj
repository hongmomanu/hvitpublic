(ns hvit.controller.index


  (:import  (org.apache.lucene.document Field Field$Store)
            (org.apache.lucene.store FSDirectory)
            (org.apache.lucene.document TextField)

            (org.apache.lucene.document Document)
            (org.apache.lucene.search IndexSearcher)
            (org.apache.lucene.queryparser.classic QueryParser)

            (org.apache.lucene.index Term IndexWriterConfig IndexWriterConfig$OpenMode IndexWriter DirectoryReader)

            (org.apache.lucene.util Version)
            (org.apache.lucene.search.highlight Highlighter TokenSources SimpleSpanFragmenter QueryScorer)
            (java.io File)
            (com.chenlb.mmseg4j.analysis ComplexAnalyzer MaxWordAnalyzer)

    )
  (:use compojure.core)
  (:use korma.core
        [korma.db :only [oracle]])


  (:require [hvit.models.db :as db]
            [noir.response :as resp]
            [hvit.models.schema :as schema]
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.deprecated :as jdeprecated]
            [hvit.views.layout :as layout]
            [hvit.controller.auth :as auth]
            )
  )

(declare addindex-func)

(def index-writer (atom {}))

(def index-searcher (atom {}))

(defn index-searchtest []
  (let [roleid (auth/getroleid nil)]

    (if (nil? roleid) (layout/render "error.html")(layout/render "indexsearch.html") )
    )

  )


(defn make-searchindex-results [searcher fileds item filedname  highlighter analyzer]
  (let [
         doc (.doc searcher (.doc item))
         stream  (TokenSources/getAnyTokenStream (.getIndexReader searcher) (.doc item) filedname  doc analyzer)
         ]

    (apply merge  (map #(assoc {} % (if (= filedname %) (.getBestFragment highlighter stream (.get doc % ))(.get doc % ) ))  fileds)) ;{:score (.score item)} )
    )

  )

(defn searchindex [text indexname]
  (try (let [
         maxnum 100
         textmap (json/read-str text)
         reqmap (get  textmap "req")
         queryfields (get reqmap "query")
         resmap (get  textmap "res")
         output (if (nil? (get resmap "output")) (assoc {} "start" 0 "limit" maxnum) (get resmap "output"))
         analyzer (new MaxWordAnalyzer)
         indexdir (str schema/datapath "index" "/" indexname)
         modtime (fs/mod-time indexdir)
         fieldname (get queryfields "fieldname")

         indexsearcher (get @index-searcher indexname)
         searcher (if (or (nil? indexsearcher)(not= (:time indexsearcher) modtime)) (let [
                                                                                           dir (FSDirectory/open (new File indexdir))
                                                                                           reader     (DirectoryReader/open dir)
                                                                                           searcher (new IndexSearcher reader)]
                                             (swap! index-searcher assoc indexname
                                               {:searcher searcher :time (fs/mod-time indexdir)})
                                             searcher
                                             )(:searcher indexsearcher))

         parser (new QueryParser Version/LUCENE_43 fieldname analyzer)

         query (.parse parser (get queryfields "fieldvalue"))
         scorer (new QueryScorer query  fieldname)
         fragmenter (new SimpleSpanFragmenter scorer)
         highlighter (new Highlighter scorer)
         highlighterdo (.setTextFragmenter highlighter fragmenter)
         tds (.search searcher query maxnum)
         sd (.scoreDocs tds)
         totalsize (alength sd)

         ]
    ;(println (fs/mod-time indexdir))

    (resp/json {:sucess true
                :result (map #(make-searchindex-results searcher (get resmap "fields") % fieldname highlighter analyzer)
                          (drop (get output "start") (take (+ (get  output "limit") (get output "start"))(to-array sd))))
                :totalCount totalsize
                :recordsTotal totalsize
                :recordsFiltered totalsize
                })
    ) (catch Exception e (resp/json {:sucess false :msg (.getMessage e)})))

  )

(defn updateindex [id text indexname]
  (try (let [
         textmap (json/read-str text)

         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [  basicdir  (str schema/datapath "index" "/")
                                             tabledir (str basicdir indexname "/")
                                             analyzer (new MaxWordAnalyzer)
                                             config (new IndexWriterConfig Version/LUCENE_43 analyzer)
                                             tabledirdo (if (fs/exists? tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/APPEND)
                                                          (do (fs/mkdir tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/CREATE)))
                                             dir     (FSDirectory/open (new File tabledir))
                                            iw (new IndexWriter dir config)]
                                       (swap! index-writer assoc indexname iw)
                                       iw
                                       ) indexwriter)
         doc  (new Document)

         ]
    (doall (map #(.add doc (new TextField  (first %)  (second %) Field$Store/YES )) textmap))
    (.updateDocument iw (new Term "id" id) doc)
    (.commit iw )
    (resp/json {:success true})
    ) (catch Exception e (resp/json {:sucess false :msg (.getMessage e)})))


  )

(defn delindex [id indexname]
  (try (let [

         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [
                                            basicdir  (str schema/datapath "index" "/")
                                            tabledir (str basicdir indexname "/")
                                            analyzer (new MaxWordAnalyzer)
                                            config (new IndexWriterConfig Version/LUCENE_43 analyzer)
                                            tabledirdo (if (fs/exists? tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/APPEND)
                                                         (do (fs/mkdir tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/CREATE)))
                                            dir     (FSDirectory/open (new File tabledir))
                                            iw (new IndexWriter dir config)]
                                       (swap! index-writer assoc indexname iw)
                                       iw
                                       ) indexwriter)
         ]
    (.deleteDocuments iw (new Term "id" id))
    (.commit iw )
    (resp/json {:success true})
    ) (catch Exception e (resp/json {:sucess false :msg (.getMessage e)})))

  )
(defn oracle-map [address user pass]
  {:classname "oracle.jdbc.OracleDriver"
   :subprotocol "oracle"
   :subname (str "thin:@" address)
   :user user
   :password pass
   }
    )
(defn makeindexfromdb [dbtype address user pass table idfield indexfields indexname]
  (def  temple-db  (condp = dbtype
              "oracle" (oracle-map address user pass)
              (str "unexpected 1")))

  (try (let [
         sql        (str "select " indexfields " from " table )
         fetch-size 1000 ;; or whatever's appropriate

         cnxn       (doto (j/get-connection temple-db)
                      (.setAutoCommit false))
         stmt       (j/prepare-statement cnxn sql :fetch-size fetch-size :concurrency :read-only)

         ;results    (rest (j/query cnxn [stmt] :as-arrays? true :row-fn #(println "nonono"  %)))
         ;results    (rest (j/query cnxn [stmt] ))
         keyid    (keyword idfield)
         doresults (jdeprecated/with-query-results results [stmt] ;; binds the results to `results`
           (doseq [row results]
             ;(println row)
             (addindex-func (json/write-str (conj row {:id (get row keyid)})) indexname false)
             ))


        ]

    ;(dorun (map #(addindex-func (json/write-str (conj % {:id (get % keyid)})) indexname false) results))
    (.commit (get @index-writer indexname))
    (resp/json {:success true}

    )

  )(catch Exception e (resp/json {:sucess false :msg (.getMessage e)})))
  )

(defn addindex-func [text indexname iscommit]
  (let [
         textmap (json/read-str text)
         indexwriter (get @index-writer indexname)
         iw    (if (nil? indexwriter)(let [
                                            analyzer (new MaxWordAnalyzer)

                                            basicdir  (str schema/datapath "index" "/")
                                            tabledir (str basicdir indexname "/")
                                            config (new IndexWriterConfig Version/LUCENE_43 analyzer)
                                            tabledirdo (if (fs/exists? tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/APPEND)
                                                         (do (fs/mkdir tabledir)(.setOpenMode config IndexWriterConfig$OpenMode/CREATE)))
                                            dir     (FSDirectory/open (new File tabledir))
                                            iw (new IndexWriter dir config)]
                                       (swap! index-writer assoc indexname iw)
                                       iw
                                       ) indexwriter)
         doc  (new Document)
         ]
    (println textmap)
    (doall (map #(.add doc (new TextField  (first %)  (if (nil? (second %)) "" (str (second %))) Field$Store/YES )) textmap))
    (.addDocument iw doc)
    (when iscommit (.commit iw ))
    ;(.close iw)
    ;(.optimize iw)
    )

  )
(defn addindex [text indexname]

  (try (do (addindex-func text indexname true)
       (resp/json {:sucess true}) )
    (catch Exception e (resp/json {:sucess false :msg (.getMessage e)})))

  )



