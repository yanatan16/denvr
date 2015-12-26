(ns denvr.util)

(defmacro node->async [& forms]
  `(let [c# (cljs.core.async/chan)]
     (~@forms #(cljs.core.async/onto-chan c#
                [(vec (cljs.core/js->clj %&))]))
     c#))

(defmacro env [k]
  `(.. cljs.nodejs/process -env ~(symbol (str "-" (name k)))))
