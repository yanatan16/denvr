(ns denvr.core
  (:require [denvr.config.core :as cfg]
            [denvr.docker :as docker]))

(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defmethod run :start
  [{[envname & _] :arguments
    {dir :configdir} :top-options}]
  (let [env (cfg/read-env dir envname)]
    (docker/start-env envname env)))

(defmethod run :stop
  [{[envname & _] :arguments
    {dir :configdir} :top-options}]
  (let [env (cfg/read-env dir envname)]
    (docker/stop-env envname env)))

(defmethod run :status
  [{[envname & _] :arguments
    {dir :configdir} :top-options}]
  (let [env (cfg/read-env dir envname)]
    (docker/env-status envname env)))
