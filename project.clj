(defproject quote-clj "0.1.0-SNAPSHOT"
  :description "(quote \"clojure\")"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [postgresql/postgresql "9.1-901-1.jdbc4"]]
  :min-lein-version "2.0.0"
  :uberjar-name "quote-clj.jar"
  :main quote-clj.core
  :profiles {:dev {:main quote-clj.core/-dev-main}})
