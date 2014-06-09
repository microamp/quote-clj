(ns quote-clj.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

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
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))
