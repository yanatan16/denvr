(ns denvr.core)

(defmacro defenvmethod [k f]
  `(defmethod denvr.core/run ~k
    [{[envname# & _] :arguments
      {dir# :configdir} :top-options}]
     (denvr.core/print-results
      (~f envname#
          (denvr.config.core/read-env dir# envname#)
          (denvr.config.core/read-host dir#)))))
