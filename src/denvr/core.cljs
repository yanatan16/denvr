(ns denvr.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as a]
            [denvr.config.core]
            [denvr.util]
            [denvr.docker :as docker]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [denvr.core :refer [defenvmethod]]
                   [cljs.core.async.macros :refer [go-loop]]))


(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defenvmethod :start docker/start-env)
(defenvmethod :stop docker/stop-env)
(defenvmethod :status docker/env-status)
