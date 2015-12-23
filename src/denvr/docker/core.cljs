(ns denvr.docker.core
  (:require [cljs.core.async :as a :refer [<!]]
            [cats.core :as m :include-macros true]
            [cats.labs.channel]
            [denvr.docker.api :as api :refer-macros [with-docker]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- env-filter
  ([] {:filters "{\"label\": [\"denvr\"]}"})
  ([name] {:filters (str "{\"label\": [\"denvr='" name "'\"]}")}))

(defn- container-spec->docker
  [env-name {:keys [ports] :as spec}]
  (-> spec
      (select-keys [:image])
      (assoc :exposed-ports (into {} (map (fn [[n p]] [(str n "/" p) {}]) ports)))
      (assoc :labels {"denvr" env-name})))


(defn start-env [name {:keys [containers]} host]
  (with-docker [docker host]
    (m/mlet
     [spec (a/to-chan containers)
      :let [spec (container-spec->docker name spec)]
      container (api/docker-create-container docker spec)
      result (api/container-start container)
      info (api/container-inspect container)]
     (m/return info))))

(defn stop-env [name env host]
  (with-docker [docker host]
    (m/mlet
     [{id "Id"} (api/docker-list-containers docker (env-filter name))
      container (api/docker-get-container docker id)
      result (api/container-stop container)]
     (m/return (str "Stopped " id)))))

(defn env-status [name env host]
  (with-docker [docker host]
    (api/docker-list-containers docker (env-filter name))))
