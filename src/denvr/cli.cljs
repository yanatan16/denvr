(ns denvr.cli
  (:require [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [denvr.core :refer [run]]
            [denvr.util :refer-macros [env]]))

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
  {:error (str (usage script)
               (if (some? error) "\n" "") error
               "\nTop-level options:\n" top-summary
               "\nAvailable Subcommands:"
               (reduce #(str %1 "\n  " (first %2) ": " (second %2))
                       "" subcmd-cli-options))})

(defn- subcmd-help [{:keys [script top-summary summary subcmd]} error]
  {:error (str (usage script)
               "\n" (get-in subcmd-cli-options-map [subcmd 0])
               (if (some? error) "\n" "") error
               "\nTop-level options:\n" top-summary
               "\nSubcommand " subcmd " options:\n"
               summary)})

(defn- help [{:keys [subcmd] :as argm} error]
  (if subcmd (subcmd-help argm error)
      (top-help argm error)))

(defn- check-subcmd-args [spec args]
  (if (< (count args) (count spec))
    (str "Missing required argument "
         (name (nth spec (count args))))))


(defn parse-subcmd-args [argm subcmd raw-args]
  (let [[_ argspec optspec] (subcmd-cli-options-map subcmd)
        {:keys [options summary errors arguments]} (parse-opts raw-args optspec)
        argerr (check-subcmd-args argspec arguments)
        argm (merge argm {:options options :summary summary
                          :arguments arguments :subcmd (keyword subcmd)})]
    (cond
      errors (help argm (str/join "\n" errors))
      (:help options) (help argm nil)
      argerr (help argm argerr)
      :else argm)))

(defn parse-args [script raw-args]
  (let [{:keys [options summary errors] [subcmd & subargs] :arguments}
        (parse-opts raw-args top-level-spec :in-order true)
        argm {:script script :top-options options :top-summary summary}]
    (cond
      errors (help argm (str/join "\n" errors))
      (:help options) (help argm nil)
      (nil? subcmd) (help argm "No subcommand specified.")
      (nil? (subcmd-cli-options-map subcmd))
        (help argm (str "Subcommand " subcmd " not recognized"))
      :else (parse-subcmd-args argm subcmd subargs))))


(defn cli [script args]
  (let [{:keys [error] :as argm} (parse-args script args)]
    (if error
      (println error)
      (try
        (run argm)
        (catch js/Error e
          (println e))))))
