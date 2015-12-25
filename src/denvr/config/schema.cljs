(ns denvr.config.schema
  (:require [schema.core :as s :include-macros true
             :refer-macros [defschema]]))

(defn- all-optional-keys [m]
  (reduce-kv #(assoc %1 (s/optional-key %2) %3) {} m))

(defschema ContainerId s/Str)

(defschema Sync {})

(defschema Container
  (s/both
   (merge
    {:id ContainerId
     (s/optional-key :sync) Sync}
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
      :working_dir s/Str :build s/Str :image s/Str :dockerfile s/Str}))
   (s/conditional
    :build {:build s/Str (s/optional-key :dockerfile) s/Str
            s/Keyword s/Any}
    :else {:image s/Str
           s/Keyword s/Any})))

(defschema Environment
  {:containers [Container]})

;; Hosts config

(defschema DockerHostConfig
  {(s/optional-key :host) s/Str
   (s/optional-key :cert-path) s/Str
   (s/optional-key :tls-verify) s/Bool
   (s/optional-key :http-timeout) s/Int})

(defschema HostConfig
  {:docker DockerHostConfig})
