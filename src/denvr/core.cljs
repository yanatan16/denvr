(ns denvr.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as a]
            [denvr.config.core :as cfg]
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

(defn load-host [{{dir :configdir} :top-options :as args}]
  (assoc args :top-dir dir :host-cfg (cfg/read-host dir)))

(defn load-env [{[env-name & _] :arguments
                 dir :top-dir :as args}]
  (cond-> args
    env-name (assoc :env-name env-name
                    :env-cfg (cfg/read-env dir env-name))))

(defn load-all-envs [{dir :top-dir :as args}]
  (assoc args :envs-cfg (cfg/read-all-envs dir)))

(defenvmethod :start [{:keys [env-cfg env-name host-cfg]}]
  (util/print-results (str "Starting " env-name)
                      (docker/start-env env-name env-cfg host-cfg)))

(defenvmethod :stop [{:keys [env-cfg env-name host-cfg]}]
  (util/print-results (str "Stopping " env-name)
                      (docker/stop-env env-name env-cfg host-cfg)))

(defenvmethod :compose-file [{:keys [env-cfg env-name host-cfg]}]
  (println (docker/env->compose env-name env-cfg host-cfg)))


(defmethod run :status [args]
  (-> args load-host load-env
      (#(if (:env-cfg %) % (load-all-envs %)))
      ((fn [{:keys [env-cfg env-name host-cfg]}]
         (util/print-results (str env-name " Status")
                             (docker/env-status env-name env-cfg host-cfg))))))
