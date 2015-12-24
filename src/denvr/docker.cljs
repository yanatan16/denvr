(ns denvr.docker
  (:require [cljs.core.async :as a :refer [<!]]
            [cljs.nodejs :as nodejs]
            [cljsjs.js-yaml]
            [cats.monad.either :as e])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private spawn (.-spawn (nodejs/require "child_process")))

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

(defn- shell [cmd args opts & [stdin]]
  (let [c (a/chan)
        proc (spawn cmd (clj->js args) (clj->js opts))
        stderr (atom "")]
    (.. proc -stdout (setEncoding "utf8"))
    (.. proc -stdout (on "data" #(go (a/>! c (e/left %)))))
    (.. proc -stderr (setEncoding "utf8"))
    (.. proc -stderr (on "data" #(swap! stderr str "\n" %)))
    (.on proc "close"
         #(do (if %
                (go (->> {:cmd cmd :args args
                          :opts opts :stdin stdin
                          :code % :stderr @stderr}
                         (ex-info "Error running command")
                         e/right
                         (a/>! c))))
              (a/close! c)))
    (if stdin (.. proc -stdin (end stdin "utf8")))
    c))

(defn- compose
  "run docker-compose"
  [name env host subcmd]
  (shell "docker-compose"
         (into ["-p" name "-f" "-"] subcmd)
         {:stdio "pipe"
          :env (host-env host)}
         (env->compose env)))


(defn start-env [name env host]
  (compose name env host ["up" "-d"]))

(defn stop-env [name env host]
  (compose name env host ["stop"]))

(defn env-status [name env host]
  (compose name env host ["ps"]))
