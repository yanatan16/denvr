(ns denvr.util
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]
            [clojure.string :as string]
            [clojure.walk :refer [postwalk]]
            [cats.core :as m :include-macros true]
            [cats.context :as ctx :include-macros true]
            [cats.labs.channel :as channel])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def ^:private js-spawn (.-spawn (nodejs/require "child_process")))
(def ^:private js-http (nodejs/require "http"))
(def ^:private js-path (nodejs/require "path"))

(defn camelize-kw [k & {:keys [capitalize?]}]
  (cond-> (name k)
    capitalize? string/capitalize
    true (string/replace #"-([a-z])" #(string/capitalize (second %)))))

(defn camelize-keys
  "Recursively transforms all map keys that are keywords to CamelCase"
  [m & opts]
  (let [f (fn [[k v]] (if (keyword? k) [(apply camelize-kw k opts) v] [k v]))]
    ;; only apply to maps
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn path-join [& ds]
  (apply (.-join js-path) ds))

(defn print-results [name [stdout stderr exit]]
  (ctx/with-context
    channel/context
    (let [out (m/mappend (m/fmap (fn [s] [:out s]) stdout)
                         (m/fmap (fn [s] [:err s]) stderr))]
      (println (str name ":"))
      (go-loop []
        (let [[l s :as v] (a/<! out)]
          (if (nil? v)
            (.exit nodejs/process (a/<! exit))
            (do (if (= l :out)
                  (println s)
                  (.error js/console s))
                (recur))))))))

(defn spawn [cmd args opts & [stdin]]
  (let [stdout (a/chan) stderr (a/chan) exit (a/chan)
        proc (js-spawn cmd (clj->js args) (clj->js opts))]
    (.. proc -stdout (setEncoding "utf8"))
    (.. proc -stdout (on "data" #(go (a/>! stdout %))))
    (.. proc -stderr (setEncoding "utf8"))
    (.. proc -stderr (on "data" #(go (a/>! stderr %))))
    (.on proc "close"
         #(do (a/close! stdout)
              (a/close! stderr)
              (a/onto-chan exit [%])))
    (if stdin (.. proc -stdin (end stdin "utf8")))
    [stdout stderr exit]))

(defn stderr-drop-stty [[stdout stderr exit]]
  [stdout
   (m/fmap #(string/replace % #"stty: stdin isn't a terminal\n?" "")
           stderr)
   exit])
