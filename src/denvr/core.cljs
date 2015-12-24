(ns denvr.core
  (:require [denvr.config.core]
            [denvr.docker :as docker]
            [cljs.core.async :refer [<!]]
            [cats.monad.either :as e]
            [cljs.pprint :refer [pprint]])
  (:require-macros [denvr.core :refer [defenvmethod]]
                   [cljs.core.async.macros :refer [go-loop]]))

(defn print-results [c]
  (println "Results:\n")
  (go-loop [v (<! c)]
    (when v
      (if (e/right? v)
        (do (println (.-message @v))
            (pprint (ex-data @v)))
        (do (println @v)
            (recur (<! c)))))))

(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defenvmethod :start docker/start-env)
(defenvmethod :stop docker/stop-env)
(defenvmethod :status docker/env-status)
