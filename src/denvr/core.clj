(ns denvr.core)

(defmacro defenvmethod [k [host envs] & form]
  `(defmethod denvr.core/run ~k
    [{[envname# & _] :arguments
      {dir# :configdir} :top-options}]
     (let [~envs (if envname#
                   (denvr.config.core/read-env dir# envname#)
                   (denvr.config.core/read-all-envs dir#))
           ~host (denvr.config.core/read-host dir#)]
       ~@form)))
