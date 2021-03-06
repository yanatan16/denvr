(defproject denvr "0.1.4-SNAPSHOT"
  :description "Development Environment Reimagined.
               A CLI manager for managing and sharing
               development environment configurations."
  :url "https://github.com/yanatan16/denvr"

  :clean-targets ["build" :target-path]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/tools.cli "0.3.3"]

                 [cljsjs/js-yaml "3.3.1-0"]
                 [org.clojars.yanatan16/cats "1.3.1"]
                 [prismatic/schema "1.0.4"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.6.1"]]

  :npm {:dependencies [["source-map-support" "0.4.0"]]
        :package {:bin {"denvr" "build/main.js"}
                  :private false}}

  :profiles {:dev {:npm {:dependencies [["request" "2.67.0"]]}}}

  :release-tasks [["vcs" "assert-committed"]
                  ["clean"]
                  ["build"]
                  ["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["npm" "publish"]
                  ["change" "version"
                   "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :cljsbuild {:test-commands {"test" ["node" "target/test-node.js"]}
              :builds [{:id "main"
                        :source-paths ["src"]
                        :compiler {:main denvr.main
                                   :output-to "build/main.js"
                                   :output-dir "build/js"
                                   :optimizations :none
                                   :target :nodejs
                                   :source-map "build/main.js.map"}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :notify-command ["node" "target/test-node.js"]
                        :compiler {:main denvr.test-runner
                                   :output-to     "target/test-node.js"
                                   :target :nodejs
                                   :output-dir    "target/test-js"
                                   :optimizations :none
                                   :pretty-print  true}}]})
