(ns denvr.cli
  (:require [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]
            [cljs.pprint :refer [pprint]]
            [clojure.string :as str]
            [cats.core :as m :include-macros true]
            [cats.monad.either :refer [left right branch]]
            [denvr.core :refer [run]]
            [denvr.util :as u :refer-macros [env]]))

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
  [["start" "Start an environment up"
    [:env]
    [["-h" "--help"]]]
   ["stop" "Stop an environment's containers"
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
  (m/mlet
    [:let [[_ argspec optspec] (subcmd-cli-options-map subcmd)
           {:keys [options summary errors arguments]}
           (parse-opts raw-args optspec)]
     argm (right (merge argm {:options options :summary summary
                              :arguments arguments :subcmd (keyword subcmd)}))
     _ (if errors (left (assoc argm :error (str/join "\n" errors))) (right))
     _ (if (:help options) (left argm) (right))
     :let [argerr (check-subcmd-args argspec arguments)]
     _ (if argerr (left (assoc argm :error argerr)) (right))]
    (m/return argm)))

;; Either monad parsing of args
(defn parse-args [script raw-args]
  (m/mlet
    [:let [{:keys [options summary errors] [subcmd & subargs] :arguments}
           (parse-opts raw-args top-level-spec :in-order true)]
     argm (right {:script script :top-options options
                  :top-summary summary})
     _ (if errors (left (assoc argm :error (str/join "\n" errors))) (right))
     _ (if (:help options) (left argm) (right))
     _ (if (nil? subcmd) (left (assoc argm :error "No subcommand specified.")) (right))
     :let [subcmd-opts (subcmd-cli-options-map subcmd)]
     _ (if (nil? subcmd-opts)
         (left (assoc argm :error (str "Subcommand " subcmd " not recognized")))
         (right))
     argm- (parse-subcmd-args argm subcmd subargs)]
    (m/return argm-)))


(defn cli [script args]
  (let [either-argm (parse-args script args)]
    (branch either-argm
            #(println (help %))
            #(try (run %)
                  (catch ExceptionInfo e
                    (let [msg (.-message e)
                          data (ex-data e)]
                      (println msg)
                      (println (pprint data))))
                  (catch js/Error e (println "ERROR" (.stack e)))))))
