(ns denvr.main
  (:require [cljs.nodejs :as nodejs]
            [denvr.cli :as cli]))

(nodejs/enable-util-print!)

(defn -main [& args]
  (println "Running denvr CLI")
  (apply cli/cli args))

(set! *main-cli-fn* -main)
