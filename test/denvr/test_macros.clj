(ns denvr.test-macros)

(defmacro re-check [re s]
  (if (and (seq? re) (= (first re) 'not))
    `(nil? (re-find ~(second re) ~s))
    `(some? (re-find ~re ~s))))

(defmacro is-parse-error [re args]
  `(let [pargs# (denvr.cli/parse-args "denvr" ~args)]
     (cljs.test/is (string? (:error pargs#))
                   (str "No error found on parsing: " (pr-str ~args)))
     (cljs.test/is (nil? (:parsed pargs#))
                   (str "Returned a parsed value on: " (pr-str ~args)))
     (if (string? (:error pargs#))
       (cljs.test/is (re-check ~re (:error pargs#))
                     (str "Error check failed for error message: "
                          (:error pargs#))))))

(defmacro is-parsed [form args]
  `(let [pargs# (denvr.cli/parse-args "denvr" ~args)]
     (cljs.test/is (nil? (:error pargs#))
                   (str "Error found in parsing: " (pr-str ~args)))
     (cljs.test/is (-> (:parsed pargs#) ~form)
                   (str "Failed check for parsed: " (pr-str (:parsed pargs#))))))
