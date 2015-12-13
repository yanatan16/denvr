(ns denver.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [denver.core-test]))

(doo-tests 'denver.core-test)
