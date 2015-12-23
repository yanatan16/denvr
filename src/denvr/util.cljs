(ns denvr.util
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]
            [clojure.string :as string]
            [clojure.walk :refer [postwalk]]))

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
