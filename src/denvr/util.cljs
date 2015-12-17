(ns denvr.util
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :as a]
            [monads.core :as m :refer [Monad]]))

(defn then [ch f]
  (let [c (a/chan)]
    (a/pipeline-async 1 c #(a/pipe (f %1) %2) ch)
    c))


;; Monads

(defn failure-value [mv] (:left @mv))
(defn success-value [mv] (:right @mv))

(deftype either-monad [v]
  IDeref
  (-deref [_] v)

  Monad
  (do-result [_ v]
    (either-monad. {:right v}))
  (bind [mv f]
    (if (failure-value mv) mv (f (success-value mv)))))

(defn success
  ([] (success nil))
  ([v] (either-monad. {:right v})))

(defn failure [v]
  (either-monad. {:left v}))

(defn either
  "Monad describing computations with possible failures. Failure is
   represented by {:left error}, and valid values are {:right value}
   As soon as a step returns {:left error}, the whole computation will yield
   {:left error} as well."
  [v]
  (success v))

(defn fail-if
  ([cond- fail] (fail-if cond- fail nil))
  ([cond- fail succ]
   (if cond- (failure fail) (success succ))))
