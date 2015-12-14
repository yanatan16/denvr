(ns denvr.main
  (:require [cljs.nodejs :as nodejs]
            [denvr.cli :as cli]))

(def path (nodejs/require "path"))
(defn basename [s] (.basename path s))

(nodejs/enable-util-print!)

(defn -main []
  (let [[script & args] (js->clj (.-argv nodejs/process))]
    (if (= (basename script) "node")
      (cli/cli (str script " " (first args)) (rest args))
      (cli/cli script args))))

(set! *main-cli-fn* -main)
