(ns denvr.main
  (:require [cljs.nodejs :as nodejs]
            [denvr.cli :as cli]))

(def path (nodejs/require "path"))
(def basename #(.basename path %))

(nodejs/enable-util-print!)

(defn -main []
  (cli/cli
   (let [[script & args] (js->clj (.-argv js/process))]
     (if (= (basename script) "node")
       (rest args)
       args))))

(set! *main-cli-fn* -main)
