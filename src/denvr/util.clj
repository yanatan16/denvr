(ns denvr.util)

(defmacro node->async [& forms]
  `(let [c# (cljs.core.async/chan)]
     (~@forms #(cljs.core.async/onto-chan
                c#
                [(if %1 (throw %1) (cljs.core/js->clj %2))]))
     c#))

(defmacro env [k]
  `(.. cljs.nodejs/process -env ~(symbol (str "-" (name k)))))
