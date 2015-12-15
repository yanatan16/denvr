(ns denvr.config.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as edn]
            [schema.core :as s]
            [denvr.config.schema :as schema]))

(def ^:private fs (nodejs/require "fs"))
(def ^:private path (nodejs/require "path"))

(defn- file-readable? [file]
  (.accessSync fs (.-R_OK fs)))

(defn- read-file [file]
  (.readFileSync fs file "utf8"))

(defn- path-join [& paths]
  (.apply (.-join path) (clj->js paths)))


(defn read-env
  "Read environment configuration from a top level directory
  and an environment name"
  [dir env]
  (-> (path-join dir (name env) "env.edn")
      (#(if-not (file-readable? %)
          (throw (ex-info "Environment not readable"
                          {:env env :configdir dir}))))
      read-file
      edn/read-string))