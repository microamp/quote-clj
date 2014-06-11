(ns quote-clj.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def db (or (System/getenv "DATABASE_URL")
            "jdbc:postgresql://localhost/quote_clj?user=postgres"))

(defn create-uuid-extension []
  (jdbc/execute!
   db
   ["CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\""]))

(defn delete-table []
  (jdbc/execute!
   db
   [(str "DROP TABLE IF EXISTS quote")]))

(defn create-table []
  (jdbc/execute!
   db
   [(str "CREATE TABLE IF NOT EXISTS quote ("
         "id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), "
         "quote TEXT NOT NULL, "
         "author TEXT NOT NULL, "
         "source TEXT, "
         "link TEXT, "
         "added TIMESTAMPTZ NOT NULL DEFAULT now()"
         ")")]))

(defn create-quote [params]
  (:id (first (jdbc/query
               db
               [(str "INSERT INTO quote (quote, author, source, link) "
                     "VALUES (?, ?, ?, ?) "
                     "RETURNING id")
                (:quote params)
                (:author params)
                (:source params)
                (:link params)]))))

(defn read-quotes []
  (jdbc/query
   db
   [(str "SELECT quote, author, source, link, added "
         "FROM quote "
         "ORDER BY added")]))

(defn read-quote-by-id [id]
  (first (jdbc/query
          db
          [(str "SELECT quote, author, source, link, added "
                "FROM quote "
                "WHERE id = ?")
           id])))

(defn count-quotes []
  (:cnt (first (jdbc/query
                db
                [(str "SELECT count(*) AS cnt "
                      "FROM quote")]))))

(defn read-random-quote []
  (first (jdbc/query
          db
          [(str "SELECT quote, author, source, link "
                "FROM quote "
                "OFFSET floor(random() * ?) "
                "LIMIT 1")
           (count-quotes)])))

(defn read-file [filename]
  (->> filename
       io/resource
       slurp
       edn/read-string))

(defn reset-db []
  (delete-table)
  (create-table)
  (println (str "Quote IDs: "
                (apply str (interpose ", " (map #(str (create-quote %))
                                                (read-file "quotes.edn")))))))
