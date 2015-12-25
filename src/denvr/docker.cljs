(ns denvr.docker
  (:require [cljs.core.async :as a :refer [<!]]
            [cljs.nodejs :as nodejs]
            [cljsjs.js-yaml]
            [clojure.string :as str]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
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
  (let [stdout (a/chan) stderr (a/chan) exit (a/chan)
        proc (spawn cmd (clj->js args) (clj->js opts))]
    (.. proc -stdout (setEncoding "utf8"))
    (.. proc -stdout (on "data" #(go (a/>! stdout %))))
    (.. proc -stderr (setEncoding "utf8"))
    (.. proc -stderr (on "data" #(go (a/>! stderr %))))
    (.on proc "close"
         #(do (a/close! stdout)
              (a/close! stderr)
              (a/onto-chan exit [%])))
    (if stdin (.. proc -stdin (end stdin "utf8")))
    [stdout stderr exit]))


(defn- drop-stty-err [[stdout stderr exit]]
  (let [stderr- (m/fmap #(str/replace % #"stty: stdin isn't a terminal\n?" "")
                        stderr)]
    [stdout stderr- exit]))

(defn- compose
  "run docker-compose"
  [name env host subcmd]
  (-> (shell "docker-compose"
             (into ["-p" name "-f" "-"] subcmd)
             {:stdio "pipe"
              :env (host-env host)}
             (env->compose env))
      drop-stty-err))


(defn start-env [name env host]
  (compose name env host ["up" "-d"]))

(defn stop-env [name env host]
  (compose name env host ["stop"]))

(defn env-status [name env host]
  (compose name env host ["ps"]))
