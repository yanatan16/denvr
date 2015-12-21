(ns denvr.test-macros)

(defmacro is-parse-error [re args]
  `(let [args# ~args
         re# ~re
         pargs# (denvr.cli/parse-args "denvr" args#)]
     (cljs.test/is (cats.monad.either/left? pargs#)
                   (str "No error found on parsing: " (pr-str args#)))
     (if re#
       (cljs.test/is (re-find re# (:error @pargs#))
                     (str "Error check failed for error message: " @pargs#))
       (cljs.test/is (nil? (:error @pargs#))
                     (str "Expected help message but go error: " @pargs#)))))

(defmacro is-parsed [form args]
  `(let [args# ~args
         pargs# (denvr.cli/parse-args "denvr" args#)]
     (cljs.test/is (cats.monad.either/right? pargs#))
     (cljs.test/is (-> @pargs# ~form)
                   (str "Failed check for parsed: " (pr-str @pargs#)))))
