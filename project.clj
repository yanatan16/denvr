(defproject denvr "0.1.0-SNAPSHOT"
  :description "Development Environment Reimagined.
               A CLI manager for managing and sharing
               development environment configurations."
  :url "https://github.com/yanatan16/denvr"

  :clean-targets ["build" :target-path]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170" :classifier "aot"]

                 [org.clojure/tools.cli "0.3.3"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.6.1"]
            [lein-doo "0.1.6"]]

  :npm {:dependencies [["source-map-support" "0.4.0"]]
        :package {:bin {"denvr" "build/main.js"}}}

  :aliases {"build" ["cljsbuild" "once" "main"]
            "test" ["doo" "node" "test-node" "once"]
            "test-auto" ["doo" "node" "test-node" "auto"]}

  :profiles {:dev {:dependencies [[lein-doo "0.1.6"]]}}

  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src"]
                        :compiler {:output-to "build/main.js"
                                   :output-dir "build/js"
                                   :optimizations :advanced
                                   :target :nodejs
                                   :source-map "build/main.js.map"}}
                       {:id "test-node"
                        :source-paths ["src" "test"]
                        :compiler {:main runner
                                   :output-to     "target/test-node.js"
                                   :target :nodejs ;;; this target required for node, plus a *main* defined in the tests.
                                   :output-dir    "target/test-js"
                                   :optimizations :none
                                   :pretty-print  true}}]})
