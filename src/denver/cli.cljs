(ns denver.cli
  (:require [cljs.nodejs :as nodejs]
            [denver.core :as denver]))

(nodejs/enable-util-print!)

(defn -main [& args]
  (println "Running denver CLI")
  (denver/run))

(set! *main-cli-fn* -main)
