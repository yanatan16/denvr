(ns denvr.core)

(def version "0.1.0-SNAPSHOT")

(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)

(defmethod run :version [_]
  (println version))
