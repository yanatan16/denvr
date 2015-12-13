(defproject denver "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.4.0"]
            [lein-doo "0.1.6"]]

  :npm {:dependencies [["source-map-support" "0.4.0"]]
        :package {:bin {"denver" "build/main.js"}}}

  :aliases {"test" ["doo" "node" "test-node" "once"]
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
                        :compiler { :main denver.runner
                                   :output-to     "target/test-node.js"
                                   :target :nodejs ;;; this target required for node, plus a *main* defined in the tests.
                                   :output-dir    "target/test-js"
                                   :optimizations :none
                                   :pretty-print  true}}]})
