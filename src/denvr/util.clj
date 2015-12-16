(ns denvr.util)

(defmacro node->async [& forms]
  `(let [c# (cljs.core.async/chan)]
     (~@forms #(if %1 (throw %1)
                   (cljs.core.async.macros/go
                     (cljs.core.async/>! c# %2)
                     (cljs.core.async/close! c#))))
     c#))

(defmacro env [k]
  `(.. cljs.nodejs/process -env ~(symbol (str "-" (name k)))))
