(ns quote-clj.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]
            [clojure.java.jdbc :as jdbc]))

(def db (or (System/getenv "DATABASE_URL")
            "jdbc:postgresql://localhost/quote_clj?user=postgres"))

(defn create-table []
  (jdbc/execute!
   db
   ["CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\""])
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

(defn delete-table []
  (jdbc/execute!
   db
   [(str "DROP TABLE IF EXISTS quote")]))

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

(defn index [hdlr]
  {:status 200
   :headers {}
   :body "hello, world"})

(defroutes routes
  "Defines routes"
  (GET "/" [] index)
  (ANY "/request" [] handle-dump)
  (not-found "404"))

(def app
  "Defines middleware stack"
  (wrap-file-info
   (wrap-resource
    (wrap-params
     routes)
    "static")))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port)}))

(defn -dev-main [port]
  (delete-table)
  (create-table)
  (println (read-quote-by-id
            (create-quote "Developers know the benefits of everything and trade-offs of nothing."
                          "Rich Hickey"
                          "Simplicity Matters"
                          "http://goo.gl/fPx6Mr")))
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))
