(ns runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [denvr.cli-test]))

(doo-tests 'denvr.cli-test)
