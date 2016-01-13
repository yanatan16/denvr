(ns denvr.core
  (:require [clojure.string :as str]
            [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as a]
            [denvr.config.core :as cfg]
            [denvr.util :as util]
            [denvr.docker :as docker]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [denvr.core :refer [defenvmethod defcontainermethod]]
                   [cljs.core.async.macros :refer [go-loop]]))


(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)

(defn get-args [{[arg1 & _] :arguments {dir :configdir} :top-options :as args} type]
  (cond-> args
    true (assoc :top-dir dir)
    (= type :env) (assoc :env-name arg1)
    (= type :container)
    (merge (let [[env-name container-name]
                 (str/split arg1 #":")]
             (when-not container-name
               (throw (ex-info "Container must be specified with <env>:<container>" {})))
             {:env-name env-name
              :container-name container-name}))))

(defn load-host [{:keys [top-dir] :as args}]
  (assoc args :host-cfg (cfg/read-host top-dir)))

(defn load-env [{:keys [env-name top-dir] :as args}]
  (cond-> args
    env-name (assoc :env-cfg (cfg/read-env top-dir env-name))))

(defn if-env [{env-cfg :env-cfg :as args} f g]
  (if env-cfg (f args) (g args)))

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
  (-> args (get-args :env) load-host load-env
      (if-env identity load-all-envs)
      ((fn [{:keys [env-cfg env-name host-cfg]}]
         (util/print-results (str env-name " Status")
                             (docker/env-status env-name env-cfg host-cfg))))))

(defcontainermethod :sync [{:keys [env-cfg env-name host-cfg container-name]}]
  (println env-name ":" container-name))

(defcontainermethod :desync [{:keys [env-cfg env-name host-cfg container-name]}]
  (println env-name ":" container-name))
