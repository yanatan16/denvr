(ns denvr.docker
  (:require [cljs.core.async :as a :refer [<!]]
            [cljs.nodejs :as nodejs]
            [cljsjs.js-yaml]
            [clojure.string :as str]
            [denvr.util :as util]))


(defn- host-env [{{:keys [tls-verify host cert-path http-timeout]} :docker}]
  (let [env (.-env nodejs/process)]
    (if host (set! (.-DOCKER_HOST env) host))
    (if (some? tls-verify) (set! (.-DOCKER_TLS_VERIFY env)
                                 (if tls-verify "1" "0")))
    (if cert-path (set! (.-DOCKER_CERT_PATH env) cert-path))
    (if  http-timeout (set! (.-DOCKER_HTTP_TIMEOUT env) (str http-timeout)))
    env))

(defn- env->compose [{:keys [containers]}]
  (->> containers
       (reduce #(assoc %1 (:id %2) (dissoc %2 :id)) {})
       clj->js
       (.safeDump js/jsyaml)))

(defn- compose
  "run docker-compose"
  [name env host subcmd]
  (-> (util/spawn "docker-compose"
                  (into ["--project-name" name "--file" "-"] subcmd)
                  {:stdio "pipe"
                   :env (host-env host)}
                  (env->compose env))
      util/stderr-drop-stty))


(defn start-env [name env host]
  (compose name env host ["up" "-d"]))

(defn stop-env [name env host]
  (compose name env host ["stop"]))

(defn env-status [name env host]
  (compose name env host ["ps"]))
