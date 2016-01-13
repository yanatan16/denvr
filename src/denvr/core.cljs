(ns denvr.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as a]
            [denvr.config.core]
            [denvr.util :as util]
            [denvr.docker :as docker]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [denvr.core :refer [defenvmethod]]
                   [cljs.core.async.macros :refer [go-loop]]))


(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defenvmethod :start [host envs]
  (run! (fn [[name env]]
          (util/print-results (str "Starting " name)
                              (docker/start-env name env host)))
        envs))

(defenvmethod :stop [host envs]
  (run! (fn [[name env]]
          (util/print-results (str "Stopping " name)
                              (docker/stop-env name env host)))
        envs))

(defenvmethod :status [host envs]
  (run! (fn [[name env]]
          (util/print-results name
                              (docker/env-status name env host)))
        envs))

(defenvmethod :compose-file [host envs]
  (run! (fn [[name env]]
          (println (docker/env->compose name env host)))
        envs))
