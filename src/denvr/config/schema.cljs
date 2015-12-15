(ns denvr.config.schema
  (:require [schema.core :as s :include-macros true
             :refer-macros [defschema]]))

(defschema ContainerId s/Str)

(defschema Image
  "An image in docker image url syntax [repository/]name[:tag]"
  s/Str)

(defschema Container
  {:id ContainerId
   :image Image})

(defschema Environment
  {:containers [Container]})
