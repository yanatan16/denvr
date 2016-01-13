(ns denvr.config.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as edn]
            [schema.core :as s]
            [denvr.config.schema :refer [HostConfig Environment]]))

(def ^:private fs (nodejs/require "fs"))
(def ^:private path (nodejs/require "path"))

(defn- file-readable? [file]
  (try (.accessSync fs file (.-R_OK fs))
       true
       (catch js/Error e
         false)))

(defn- read-file [file]
  (.readFileSync fs file "utf8"))

(defn- path-join [& paths]
  (apply (.-join path) paths))

(defn- check-schema [cfg schema str {:as ex-data}]
  (if-let [errs (s/check schema cfg)]
    (throw (ex-info str (assoc ex-data :errors errs)))
    cfg))

(defn check-host [host & [{:as ex-data}]]
  (check-schema host HostConfig "Host configuration is invalid" ex-data))

(defn check-env [env & [{:as ex-data}]]
  (check-schema env Environment "Environment configuration is invalid" ex-data))

(defn read-host
  "Read host configuration from a top level config directory"
  [dir]
  (some-> (path-join dir "host.edn")
          (#(if (file-readable? %) %))
          read-file
          edn/read-string
          (check-host {:configdir dir})))

(defn read-env
  "Read environment configuration from a top level directory
  and an environment name"
  [dir env]
  (-> (path-join dir env "env.edn")
      (#(if-not (file-readable? %)
          (throw (ex-info "Environment not readable"
                          {:env env :configdir dir}))
          %))
      read-file
      edn/read-string
      (check-env {:env env :configdir dir})
      (#(hash-map env %))))

(defn read-all-envs
  "Read all available environment configurations from a top level directory"
  [dir]
  (apply merge
         (for [env (.readdirSync fs dir)
               :when (not (re-find #"\." env))]
           (read-env dir env))))
