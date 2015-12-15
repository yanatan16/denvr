(ns denvr.core
  (:require [denvr.config.core :as cfg]))

(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defmethod run :up
  [{[env & _] :arguments
    {dir :configdir} :top-options}]
  (throw (ex-info "Test" {:show-help true})))
