(ns denver.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [denver.core :as denver]))

(deftest failing-test
  (is (= :success (denver/run))))
