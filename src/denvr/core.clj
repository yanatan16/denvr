(ns denvr.core)



(defmacro defenvmethod [k & form]
  `(defmethod denvr.core/run ~k [args#]
    (-> args# denvr.core/load-host denvr.core/load-env
        ((fn ~@form)))))
