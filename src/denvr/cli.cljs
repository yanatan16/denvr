(ns denvr.cli
  (:require [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [monads.core :as m]
            [denvr.core :refer [run]]
            [denvr.util :as u :refer-macros [env]])
  (:require-macros [monads.macros :as mm]))

(def top-level-spec
  [["-c" "--configdir DIR" "Configuration Directory"
    :id :configdir
    :default (str (env "HOME") "/.denvr")]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(def subcmd-cli-options
  [["up" "Bring an environment up"
    [:env]
    [["-h" "--help"]]]
   ["down" "Shut an environment down"
    [:env]
    [["-h" "--help"]]]
   ["status" "Query the status of environments"
    []
    [["-h" "--help"]]]
   ["sync" "Start code-sync of a single container"
    [:container]
    [["-h" "--help"]]]
   ["unsync" "Stop code-sync of a single container"
    [:container]
    [["-h" "--help"]]]
   ["rebuild" "Rebuild a single container and update local environment"
    [:container]
    [["-h" "--help"]]]
   ["push" "Push an environment to a remote repository"
    [:env]
    [["-h" "--help"]]]
   ["pull" "Pull an updated environment from a remote repository"
    [:env]
    [["-h" "--help"]]]
   ["clone" "Clone an environment from a remote repository"
    [:url]
    [["-h" "--help"]]]])

(defn usage [script]
  (str "Usage: " script " [top-options] subcmd [subcmd-options|subcmd-args]"))

(def subcmd-cli-options-map
  (reduce #(assoc %1 (first %2) (rest %2))
          {} subcmd-cli-options))

(defn- top-help [{:keys [script top-summary]} error]
  (str (usage script)
       (if (some? error) "\n" "") error
       "\nTop-level options:\n" top-summary
       "\nAvailable Subcommands:"
       (reduce #(str %1 "\n  " (first %2) ": " (second %2))
               "" subcmd-cli-options)))

(defn- subcmd-help [{:keys [script top-summary summary subcmd]} error]
  (str (usage script)
       "\n" (get-in subcmd-cli-options-map [subcmd 0])
       (if (some? error) "\n" "") error
       "\nTop-level options:\n" top-summary
       "\nSubcommand " subcmd " options:\n"
       summary))

(defn- help [{:keys [subcmd error] :as argm}]
  (if subcmd
    (subcmd-help argm error)
    (top-help argm error)))

(defn- check-subcmd-args [spec args]
  (if (< (count args) (count spec))
    (str "Missing required argument "
         (name (nth spec (count args))))))

;; Either monad parsing of subcmd args
(defn parse-subcmd-args [argm subcmd raw-args]
  (mm/do
    u/either
    [:let [[_ argspec optspec] (subcmd-cli-options-map subcmd)
           {:keys [options summary errors arguments]}
           (parse-opts raw-args optspec)]
     argm (u/success (merge argm {:options options :summary summary
                                  :arguments arguments :subcmd (keyword subcmd)}))
     _ (u/fail-if errors (assoc argm :error (str/join "\n" errors)))
     _ (u/fail-if (:help options) argm)
     :let [argerr (check-subcmd-args argspec arguments)]
     _ (u/fail-if argerr (assoc argm :error argerr))]
    argm))

;; Either monad parsing of args
(defn parse-args [script raw-args]
  (mm/do
    u/either
    [:let [{:keys [options summary errors] [subcmd & subargs] :arguments}
           (parse-opts raw-args top-level-spec :in-order true)]
     argm (u/success {:script script :top-options options
                      :top-summary summary})
     _ (u/fail-if errors (assoc argm :error (str/join "\n" errors)))
     _ (u/fail-if (:help options) argm)
     _ (u/fail-if (nil? subcmd) (assoc argm :error "No subcommand specified."))
     :let [subcmd-opts (subcmd-cli-options-map subcmd)]
     _ (u/fail-if (nil? subcmd-opts)
                  (assoc argm :error (str "Subcommand " subcmd " not recognized")))
     argm- (parse-subcmd-args argm subcmd subargs)]
    argm-))


(defn cli [script args]
  (let [either (parse-args script args)]
    (if (u/failure-value either)
      (println (help (u/failure-value either)))
      (try
        (run (u/success-value either))
        (catch js/Error e
          (println e))))))
