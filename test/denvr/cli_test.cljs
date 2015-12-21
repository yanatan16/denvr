(ns denvr.cli-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [denvr.cli :refer [parse-args]]
            [denvr.util :refer-macros [env]]
            [cats.monad.either])
  (:require-macros [denvr.test-macros :refer [is-parse-error is-parsed]]))

(def default-top-options {:configdir (str (env "HOME") "/.denvr") :verbosity 0})
(defn empty-parsed []
  {:subcmd nil
   :options {}
   :arguments []
   :top-options default-top-options
   :top-summary (:top-summary @(parse-args "denvr" ["-h"]))
   :summary "  -h, --help"
   :script "denvr"})
(defn status-parsed []
  (assoc (empty-parsed) :subcmd :status))
(defn up-parsed []
  (assoc (empty-parsed) :subcmd :up :arguments ["env"]))

(deftest top-level-parse-args-test
  (testing "top level parse-args help"
    (is-parse-error nil ["-h"])
    (is-parse-error nil ["--help"]))
  (testing "no subcommand"
    (is-parse-error #"No subcommand specified" []))
  (testing "bad subcommand"
    (is-parse-error #"Subcommand foo not recognized" ["foo"]))
  (testing "Configuration Directory option"
    (is-parsed (= (assoc-in (status-parsed) [:top-options :configdir]
                            "/tmp/configdir"))
               ["-c" "/tmp/configdir" "status"])
    (is-parsed (= (assoc-in (status-parsed) [:top-options :configdir]
                               "/tmp/configdir"))
               ["--configdir" "/tmp/configdir" "status"]))
  (testing "Verbosity option"
    (is-parsed (= (assoc-in (status-parsed) [:top-options :verbosity] 1))
               ["-v" "status"])
    (is-parsed (= (assoc-in (status-parsed) [:top-options :verbosity] 3))
               ["-v" "-v" "-v" "status"])
    (is-parsed (= (assoc-in (status-parsed) [:top-options :verbosity] 3))
               ["-vvv" "status"]))
  (testing "status subcommand"
    (is-parsed (= (status-parsed)) ["status"]))
  (testing "up subcommand"
    (is-parse-error #"Missing required argument env" ["up"])
    (is-parsed (= (up-parsed)) ["up" "env"])))
