(ns denvr.test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-all-tests]]
            [denvr.cli-test]
            [denvr.docker-test]))

(nodejs/enable-util-print!)


(set! *main-cli-fn* #(run-all-tests #"denvr\..*test"))
