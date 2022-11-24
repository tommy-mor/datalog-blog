(defproject jsonapi-xtdb "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.xtdb/xtdb-core "1.22.0"]
                 [com.xtdb/xtdb-rocksdb "1.22.0"]
                 [hato "0.8.2"]
                 [cheshire "5.11.0"]
                 [nextjournal/clerk "6c0a44a5634afdec511469d7220158b2957bf488"]]
  :repl-options {:init-ns jsonapi-xtdb.core}
  :plugins [[reifyhealth/lein-git-down "0.4.1"]]
  :repositories [["public-github" {:url "git://github.com"}]]
  


  )
