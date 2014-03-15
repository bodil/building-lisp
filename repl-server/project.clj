(defproject org.bodil/repl-server "0.1.0"
  :description "Clojure REPL server for slides"
  :url "https://github.com/bodil/building-lisp"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.17"]
                 [cheshire "5.3.1"]
                 [instaparse "1.2.16"]
                 [org.clojure/core.logic "0.8.7"]
                 [org.clojure/core.match "0.2.1"]]
  :jvm-opts ["-Dfile.encoding=utf-8"
             "-Djava.security.policy=java.policy"]
  :main repl.core)
