(ns denvr.docker
  (:require [cljs.core.async :as a :refer [<!]]
            [cljs.nodejs :as nodejs]
            [cljsjs.js-yaml]
            [clojure.string :as str]
            [denvr.util :as util]))

(defn- ->yaml [o] (.safeDump js/jsyaml (clj->js o)))

(defn- host-env [{{:keys [tls-verify host cert-path http-timeout]} :docker}]
  (let [env (.-env nodejs/process)]
    (if host (set! (.-DOCKER_HOST env) host))
    (if (some? tls-verify) (set! (.-DOCKER_TLS_VERIFY env)
                                 (if tls-verify "1" "0")))
    (if cert-path (set! (.-DOCKER_CERT_PATH env) cert-path))
    (if  http-timeout (set! (.-DOCKER_HTTP_TIMEOUT env) (str http-timeout)))
    env))

(defn denvr-cfg
  [{:keys [type image sync tag version repo build dockerfile sync-dir]
    :as denvr}
   id dir]
  (if (and (= type :variable) (not dir))
    (throw (ex-info "No host directory for variable container"
                    {:container-id id :denvr-config denvr})))
  (cond
    (= type :stable) {:image image}
    sync {:build (util/path-join dir (or build "."))
          :environment {"SYNC" "1"}
          :volumes [(str (util/path-join dir (or build ".")) ":" sync-dir)]}
    :else {:image (str repo tag ":" version)}))

(defn env->compose-xf [dirs]
  (map (fn [{:keys [id compose denvr]}]
         [id (merge compose (denvr-cfg denvr id (get dirs id)))])))

(defn env->compose [name {:keys [containers]} {dirs-cfg :dirs}]
  (->yaml (into {} (env->compose-xf (get dirs-cfg name)) containers)))

(defn- compose
  "run docker-compose"
  [name env host subcmd]
  (-> (util/spawn "docker-compose"
                  (into ["--project-name" name "--file" "-"] subcmd)
                  {:stdio "pipe"
                   :env (host-env host)}
                  (env->compose name env host))
      util/stderr-drop-stty))


(defn start-env [name env host]
  (compose name env host ["up" "-d"]))

(defn stop-env [name env host]
  (compose name env host ["stop"]))

(defn env-status [name env host]
  (compose name env host ["ps"]))
