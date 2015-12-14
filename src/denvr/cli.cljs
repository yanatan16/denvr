(ns denvr.cli
  (:require [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [denvr.core :refer [run]]))

(def top-level-spec
  [["-c" "--configdir DIR" "Configuration Directory"
    :id :configdir
    :default "~/.denvr"]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(def subcmd-cli-options
  [["version" "Report version of denvr" []]
   ["up" "Bring an environment up"
    [["-h" "--help"]]]
   ["down" "Shut an environment down"
    [["-h" "--help"]]]
   ["status" "Query the status of environments"
    [["-h" "--help"]]]
   ["sync" "Start code-sync of a single container"
    [["-h" "--help"]]]
   ["unsync" "Stop code-sync of a single container"
    [["-h" "--help"]]]
   ["rebuild" "Rebuild a single container and update local environment"
    [["-h" "--help"]]]
   ["push" "Push an environment to a remote repository"
    [["-h" "--help"]]]
   ["pull" "Pull an updated environment from a remote repository"
    [["-h" "--help"]]]
   ["clone" "Clone an environment from a remote repository"
    [["-h" "--help"]]]])

(defn usage [script]
  (str "Usage: " script " [top-options] subcmd [subcmd-options]"))

(def subcmd-cli-options-map
  (reduce #(assoc %1 (first %2) (last %2))
          {} subcmd-cli-options))

(defn- top-help [script top-summary error]
  {:error (str (usage script)
               (if (some? error) "\n" "")
               error
               "\nTop-level options:\n"
               top-summary
               "\nAvailable Subcommands:"
               (reduce #(str %1 "\n  " (first %2) ": " (second %2))
                       "" subcmd-cli-options))})

(defn- subcmd-help [script top-summary subcmd-summary error subcmd]
  {:error (str (usage script)
               (if (some? error) "\n" "")
               error
               "\nTop-level options:\n" top-summary
               "\nSubcommand " subcmd " options:\n"
               subcmd-summary)})

(defn parse-subcmd-args [script subcmd args top-summary top-options]
  (let [[subcmd-docstr subcmd-spec] (subcmd-cli-options-map subcmd)
        {:keys [options summary errors arguments]}
        (parse-opts args subcmd-spec)]
    (cond
      errors (subcmd-help script top-summary summary (str/join "\n" errors) subcmd)
      (:help options) (subcmd-help script top-summary summary nil subcmd)
      :else {:parsed {:subcmd (keyword subcmd)
                      :top-options top-options
                      :options options
                      :arguments arguments}})))

(defn parse-args [script args]
  (let [{:keys [options summary errors]
         [subcmd & subargs] :arguments}
        (parse-opts args top-level-spec :in-order true)]
    (cond
      errors (top-help script summary (str/join "\n" errors))
      (:help options) (top-help script summary nil)
      (nil? subcmd) (top-help script summary "No subcommand specified.")
      (nil? (subcmd-cli-options-map subcmd))
        (top-help script summary (str "Subcommand " subcmd " not recognized"))
      :else (parse-subcmd-args script subcmd subargs summary options)
      )))


(defn cli [script args]
  (let [{:keys [error parsed]} (parse-args script args)]
    (if error
      (println error)
      (run parsed))))
