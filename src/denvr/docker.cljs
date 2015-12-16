(ns denvr.docker
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]
            [denvr.util :refer [then] :refer-macros [node->async]]))

(def ^:private Docker (nodejs/require "dockerode"))

(defn- docker-create-container [docker spec]
  (node->async .createContainer docker (clj->js spec)))

(defn- container-start [container]
  (node->async .start container))


(defn- start-container [docker {:keys [id image]}]
  (then (docker-create-container docker {:name id :image image})
        #(container-start %)))

(defn start-env [{:keys [containers]}]
  (let [docker (Docker.)]
    (a/merge (map #(start-container docker %) containers))))
