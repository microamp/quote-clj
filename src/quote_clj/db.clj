(ns quote-clj.db
  (:require
   [clojure.java.jdbc :as jdbc]))

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
         "by TEXT NOT NULL, "
         "source TEXT, "
         "link TEXT, "
         "added TIMESTAMPTZ NOT NULL DEFAULT now()"
         ")")]))

(defn create-quote [quote by source link]
  (:id (first (jdbc/query
               db
               [(str "INSERT INTO quote (quote, by, source, link) "
                     "VALUES (?, ?, ?, ?) "
                     "RETURNING id")
                quote by source link]))))

(defn read-quotes []
  (jdbc/query
   db
   [(str "SELECT quote, by, source, link, added "
         "FROM quote "
         "ORDER BY added")]))

(defn read-quote-by-id [id]
  (first (jdbc/query
          db
          [(str "SELECT quote, by, source, link, added "
                "FROM quote "
                "WHERE id = ?")
           id])))
