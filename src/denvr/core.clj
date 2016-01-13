(ns denvr.core)


(defmacro defenvmethod [k & form]
  `(defmethod denvr.core/run ~k [args#]
     (-> args# (denvr.core/get-args :env) denvr.core/load-host denvr.core/load-env
         ((fn ~@form)))))

(defmacro defcontainermethod [k & form]
  `(defmethod denvr.core/run ~k [args#]
     (-> args# (denvr.core/get-args :container) denvr.core/load-host
         denvr.core/load-env
         ((fn ~@form)))))
