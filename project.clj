(defproject denvr "0.1.4-SNAPSHOT"
  :description "Development Environment Reimagined.
               A CLI manager for managing and sharing
               development environment configurations."
  :url "https://github.com/yanatan16/denvr"

  :clean-targets ["build" :target-path]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170" :classifier "aot"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/tools.cli "0.3.3"]

                 [net.clojure/monads "1.0.2"]
                 [prismatic/schema "1.0.4"]]


  :profiles {:dev {:dependencies [[lein-doo "0.1.6"]]}}

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.6.1"]
            [lein-doo "0.1.6"]]

  :npm {:dependencies [["source-map-support" "0.4.0"]
                       ["dockerode" "2.2.7"]]
        :package {:bin {"denvr" "build/main.js"}
                  :private false}}

  :aliases {"build" ["cljsbuild" "once" "main"]
            "build-auto" ["cljsbuild" "auto" "main"]
            "test" ["doo" "node" "test-node" "once"]
            "test-auto" ["doo" "node" "test-node" "auto"]}

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


  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src"]
                        :compiler {:main denvr.main
                                   :output-to "build/main.js"
                                   :output-dir "build/js"
                                   :optimizations :none
                                   :target :nodejs
                                   :source-map "build/main.js.map"}}
                       {:id "test-node"
                        :source-paths ["src" "test"]
                        :compiler {:main runner
                                   :output-to     "target/test-node.js"
                                   :target :nodejs
                                   :output-dir    "target/test-js"
                                   :optimizations :none
                                   :pretty-print  true}}]})
