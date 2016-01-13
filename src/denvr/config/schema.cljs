(ns denvr.config.schema
  (:require [schema.core :as s :include-macros true
             :refer-macros [defschema]
             :refer [optional-key named enum]]))

(defn- all-optional-keys [m]
  (reduce-kv #(assoc %1 (optional-key %2) %3) {} m))

(defschema EnvName s/Str)
(defschema ContainerId s/Str)

(defschema DenvrConfig
  (s/conditional
   #(= (:type %) :variable)
    {:type (enum :variable)
     :sync (named s/Bool "Whether sync is turned on")
     :sync-dir (named s/Str "Inner container directory to sync with")
     :tag (named s/Str "Image tag")
     :version (named s/Str "Image version")
     (optional-key :repo) (named s/Str "Repository URL")
     (optional-key :build) (named s/Str "Build directory. Default: .")
     (optional-key :dockerfile) (named s/Str "Dockerfile if not default")}
    :else {:type (enum :stable)
           :image (named s/Str "Image URL")}))

(defschema ComposeConfig
  (all-optional-keys
   {:cap_add [s/Str] :cap_drop [s/Str] :command (s/cond-pre s/Str [s/Str])
    :cgroup_parent s/Str :container_name s/Str :devices [s/Str]
    :dns (s/cond-pre s/Str [s/Str]) :dns-search (s/cond-pre s/Str [s/Str])
    :env_file (s/cond-pre s/Str [s/Str])
    :environment (s/cond-pre [s/Str] {s/Str s/Str})
    :expose [(s/cond-pre s/Str s/Int)] :extends {:service s/Str}
    :extra_hosts [s/Str] :labels (s/cond-pre [s/Str] {s/Str s/Str})
    :links [s/Str] :log_driver s/Str :log_opt {s/Str s/Str}
    :net s/Str :pid s/Str :ports [(s/cond-pre s/Str s/Int)]
    :security_opt [s/Str] :ulimits {s/Str s/Any}
    :volumes [s/Str] :volume_driver s/Str :volumes_from [s/Str]
    :cpu_shares s/Str :cpuset s/Str :domainname s/Str :entrypoint s/Str
    :hostname s/Str :ipc s/Str :mac_address s/Str :mem_limit s/Str
    :memswap_limit s/Str :privileged s/Str :read_only s/Str
    :restart s/Str :stdin_open s/Str :tty s/Str :user s/Str
    :working_dir s/Str}))


(defschema Container
  "A container specification."
  {:id ContainerId
   :denvr DenvrConfig
   :compose ComposeConfig})

(defschema Environment
  {:containers [Container]})

;; Hosts config

(defschema DockerHostConfig
  (all-optional-keys
   {:host s/Str
    :cert-path s/Str
    :tls-verify s/Bool
    :http-timeout s/Int}))

(defschema HostConfig
  (all-optional-keys
   {:docker DockerHostConfig
    :dirs {EnvName {ContainerId s/Str}}}))
