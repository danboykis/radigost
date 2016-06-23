(defproject radigost "0.1.0-SNAPSHOT"
  :description "Work with JWT in Clojure"
  :url "https://github.com/danboykis/radigost"
  :dependencies [[org.clojure/clojure "1.8.0"]]

  :profiles {:dev {:source-paths ["dev" "test/resources"]
                   :repl-options {:init-ns user}
                   :dependencies [[cheshire "5.6.2"]
                                  [http-kit "2.1.18"]
                                  [org.clojure/test.check "0.9.0"]]}})

