(ns denvr.test-macros)

(defmacro is-parse-error [re args]
  `(let [pargs# (denvr.cli/parse-args "denvr" ~args)
         err# (denvr.util/failure-value pargs#)]
     (cljs.test/is (some? err#)
                   (str "No error found on parsing: " (pr-str ~args)))
     (if ~re
       (cljs.test/is (re-find ~re (:error err#))
                     (str "Error check failed for error message: " err#))
       (cljs.test/is (nil? (:error err#))
                     (str "Expected help message but go error: " err#)))))

(defmacro is-parsed [form args]
  `(let [pargs# (denvr.cli/parse-args "denvr" ~args)
         val# (denvr.util/success-value pargs#)]
     (cljs.test/is (-> val# ~form)
                   (str "Failed check for parsed: " (pr-str val#)
                        " for " (quote ~form)))))
