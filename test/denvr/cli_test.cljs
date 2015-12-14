(ns denvr.cli-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [denvr.cli :refer [parse-args]])
  (:require-macros [denvr.test-macros :refer [is-parse-error is-parsed]]))

(def default-top-options {:configdir "~/.denvr" :verbosity 0})
(def empty-parsed {:subcmd nil :options {} :arguments []
                   :top-options default-top-options})
(def version-parsed (assoc empty-parsed :subcmd :version))

(deftest top-level-parse-args-test
  (testing "top level parse-args help"
    (is-parse-error (not #"No subcommand specified") ["-h"])
    (is-parse-error (not #"No subcommand specified") ["--help"]))
  (testing "no subcommand"
    (is-parse-error #"No subcommand specified" []))
  (testing "bad subcommand"
    (is-parse-error #"Subcommand foo not recognized" ["foo"]))
  (testing "version subcommand"
    (is-parsed (= version-parsed) ["version"]))
  (testing "Configuration Directory option"
    (is-parsed (= (assoc-in version-parsed [:top-options :configdir]
                               "/tmp/configdir"))
               ["-c" "/tmp/configdir" "version"])
    (is-parsed (= (assoc-in version-parsed [:top-options :configdir]
                               "/tmp/configdir"))
               ["--configdir" "/tmp/configdir" "version"]))
  (testing "Verbosity option"
    (is-parsed (= (assoc-in version-parsed [:top-options :verbosity] 1))
               ["-v" "version"])
    (is-parsed (= (assoc-in version-parsed [:top-options :verbosity] 3))
               ["-v" "-v" "-v" "version"])
    (is-parsed (= (assoc-in version-parsed [:top-options :verbosity] 3))
               ["-vvv" "version"])))
