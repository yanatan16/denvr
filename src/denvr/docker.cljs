(ns denvr.docker
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a :refer [<!]]
            [denvr.util :refer-macros [node->async] :refer [camelize-keys]]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [denvr.docker :refer [with-docker]]
                   [cljs.core.async.macros :refer [go-loop]]))

(def ^:private fs (nodejs/require "fs"))
(def ^:private split-ca (nodejs/require "split-ca"))
(def ^:private Docker (nodejs/require "dockerode"))

(defn make-docker [{{:keys [ca cert key] :as host-cfg} :docker}]
  (-> (if (and ca cert key)
        (merge host-cfg {:ca (split-ca ca)
                         :cert (.readFileSync fs cert)
                         :key (.readFileSync fs key)})
        (dissoc host-cfg :ca :cert :key))
      clj->js
      Docker.))

(defn- clj->docker [opts]
  (-> opts camelize-keys clj->js))

(defn- docker-create-container [docker spec]
  (node->async .createContainer docker (clj->docker spec)))

(defn- docker-list-containers
  ([docker] (docker-list-containers docker {}))
  ([docker opts]
   (m/bind (node->async .listContainers docker (clj->docker opts))
           a/to-chan)))


(defn- docker-get-container [docker container-id]
  (a/to-chan [(.getContainer docker container-id)]))

(defn- container-start [container]
  (node->async .start container))

(defn- container-stop [container]
  (node->async .stop container))

(defn- container-inspect [container]
  (node->async .inspect container))

(defn- container-rename [container name]
  (node->async .rename container name))

(defn- env-filter
  ([] {:filters "{\"label\": [\"denvr\"]}"})
  ([name] {:filters (str "{\"label\": [\"denvr='" name "'\"]}")}))

(defn start-env [name {:keys [containers]} host]
  (with-docker [docker host]
    (m/mlet
     [spec (a/to-chan containers)
      :let [spec (assoc spec :labels {"denvr" name})]
      container (docker-create-container docker spec)
      result (container-start container)
      info (container-inspect container)]
     (m/return info))))

(defn stop-env [name env host]
  (with-docker [docker host]
    (m/mlet
     [{id "Id"} (docker-list-containers docker (env-filter name))
      container (docker-get-container docker id)
      result (container-stop container)]
     (m/return (str "Stopped " id)))))

(defn env-status [name env host]
  (with-docker [docker host]
    (docker-list-containers docker (env-filter name))))
