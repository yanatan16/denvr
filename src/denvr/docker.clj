(ns denvr.docker)

(defmacro with-docker [[s host] & forms]
  `(let [~s (denvr.docker/make-docker ~host)]
    ~@forms))
