(ns denvr.docker.api
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a :refer [<!]]
            [cats.core :as m :include-macros true]
            [denvr.util :refer-macros [node->async] :refer [camelize-keys]]))

(def ^:private fs (nodejs/require "fs"))
(def ^:private split-ca (nodejs/require "split-ca"))
(def ^:private Docker (nodejs/require "dockerode"))

(defn make-docker [{{:keys [ca cert key] :as host-cfg} :docker}]
  (-> (if (and ca cert key)
        (merge host-cfg {:ca (split-ca ca)
                         :cert (.readFileSync fs cert)
                         :key (.readFileSync fs key)})
        (dissoc host-cfg :ca :cert :key))
      (camelize-keys :capitalize? false)
      clj->js
      Docker.))

(defn- clj->docker [opts]
  (-> opts (camelize-keys :capitalize? true) clj->js))

(defn docker-create-container [docker spec]
  (node->async .createContainer docker (clj->docker spec)))

(defn docker-list-containers
  ([docker] (docker-list-containers docker {}))
  ([docker opts]
   (m/bind (node->async .listContainers docker (clj->docker opts))
           a/to-chan)))


(defn docker-get-container [docker container-id]
  (a/to-chan [(.getContainer docker container-id)]))

(defn container-start [container]
  (node->async .start container))

(defn container-stop [container]
  (node->async .stop container))

(defn container-inspect [container]
  (node->async .inspect container))

(defn container-rename [container name]
  (node->async .rename container name))
