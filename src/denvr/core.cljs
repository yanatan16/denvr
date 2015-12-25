(ns denvr.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :as a]
            [denvr.config.core]
            [denvr.docker :as docker]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [denvr.core :refer [defenvmethod]]
                   [cljs.core.async.macros :refer [go-loop]]))

(defn print-results [stdout stderr exit]
  (let [out (m/mappend (m/fmap (fn [s] [:out s]) stdout)
                       (m/fmap (fn [s] [:err s]) stderr))]
    (go-loop []
      (let [[l s :as v] (a/<! out)]
        (if (nil? v)
          (.exit nodejs/process (a/<! exit))
          (do (if (= l :out)
                (println s)
                (.error js/console s))
              (recur)))))))

(defmulti run
  "Run a subcommand.
  Called with a map of keys :top-options, :options, :arguments"
  :subcmd)


(defenvmethod :start docker/start-env)
(defenvmethod :stop docker/stop-env)
(defenvmethod :status docker/env-status)
