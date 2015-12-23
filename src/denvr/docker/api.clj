(ns denvr.docker.api)

(defmacro with-docker [[s host] & forms]
  `(let [~s (denvr.docker.api/make-docker ~host)]
    ~@forms))
