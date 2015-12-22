(ns denvr.docker
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a :refer [<!]]
            [denvr.util :refer-macros [node->async] :refer [camelize-keys]]
            [cats.core :as m :include-macros true]
            [cats.labs.channel])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ^:private Docker (nodejs/require "dockerode"))


(defn- clj->docker [opts]
  (-> opts camelize-keys clj->js))

(defn- docker-create-container [docker spec]
  (node->async .createContainer docker (clj->docker spec)))

(defn- docker-list-containers
  ([docker] (docker-list-containers docker {}))
  ([docker opts]
   (m/bind (node->async .listContainers docker (clj->docker opts))
           #(if (empty? %)
              (throw (js/Error. "No containers detected"))
              (a/to-chan %)))))

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

(defn print-results [c]
  (println "Results:\n")
  (go-loop [v (<! c)]
    (when v (println v)
            (recur (<! c)))))

(defn start-env [name {:keys [containers]}]
  (print-results
   (m/mlet
    [:let [docker (Docker.)]
     spec (a/to-chan containers)
     :let [spec (assoc spec :labels {"denvr" name})]
     container (docker-create-container docker spec)
     result (container-start container)
     info (container-inspect container)]
    (m/return info))))

(defn stop-env [name _]
  (print-results
   (m/mlet
    [:let [docker (Docker.)]
     {id "Id"} (docker-list-containers docker (env-filter name))
     container (docker-get-container docker id)
     result (container-stop container)]
    (m/return (str "Stopped " id)))))

(defn env-status [name _]
  (print-results
   (m/mlet
    [:let [docker (Docker.)]
     container (docker-list-containers docker (env-filter name))]
    (if container
      (m/return container)
      (m/return "No environment running")))))
