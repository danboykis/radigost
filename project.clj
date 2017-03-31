(defproject radigost "0.1.1"
  :description "Work with JWT in Clojure"
  :url "https://github.com/danboykis/radigost"
  :license {:name "Unlicense" :url "http://unlicense.org/"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]]

  :profiles {:dev {:source-paths ["dev" "test/resources"]
                   :repl-options {:init-ns user}
                   :dependencies [[cheshire "5.6.2"]
                                  [http-kit "2.1.18"]
                                  [org.clojure/test.check "0.9.0"]]}})

