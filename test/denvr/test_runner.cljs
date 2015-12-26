(ns denvr.test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-tests]]
            [denvr.cli-test]
            [denvr.docker-test]))

(nodejs/enable-util-print!)

(defn run []
  (run-tests 'denvr.cli-test
             'denvr.docker-test))

(set! *main-cli-fn* run)
