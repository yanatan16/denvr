(ns denvr.config.schema
  (:require [schema.core :as s :include-macros true
             :refer-macros [defschema]]))

(defschema ContainerId s/Str)

(defschema Image
  "An image in docker image url syntax [repository/]name[:tag]"
  s/Str)

(defschema Ports
  "A list of exposed ports"
  [[(s/one s/Int "Port Number")
    (s/one (s/enum :tcp :udp) "Protocol")]])

(defschema Container
  {:id ContainerId
   :image Image
   (s/optional-key :ports) Ports})

(defschema Environment
  {:containers [Container]})

;; Hosts config

(defschema DockerHostConfig
  {(s/optional-key :socket-path) s/Str
   (s/optional-key :host) s/Str
   (s/optional-key :port) s/Num
   (s/optional-key :ca) s/Str
   (s/optional-key :cert) s/Str
   (s/optional-key :key) s/Str})

(defschema HostConfig
  {:docker DockerHostConfig})
