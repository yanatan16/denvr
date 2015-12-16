(ns denvr.util
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]))

(defn then [ch f]
  (let [c (a/chan)]
    (a/pipeline-async 1 c #(a/pipe (f %1) %2) ch)
    c))
