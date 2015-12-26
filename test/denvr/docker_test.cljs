(ns denvr.docker-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [<!] :as a]
            [clojure.string :as str]
            [denvr.util :refer-macros [env node->async]]
            [denvr.docker :as docker]
            [denvr.cli :as cli]
            [denvr.config.core :as cfg])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def request (nodejs/require "request"))

(def configdir (str (env "HOME") "/.denvr"))
(def host (cfg/read-host configdir))
(def env
  {:containers [{:id "nginx"
                 :image "nginx:1.9.8"
                 :ports ["48080:80"]}]})
(def docker-host
  (->> (get-in host [:docker :host])
       (re-find #"\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}")))

(deftest start-status-stop-test
  (async
   done
   (go (let [name (str (gensym "test"))
             id' (atom nil)]
         (testing "validate env"
           (is (= env (cfg/check-env env))))
         (testing "start env"
           (let [[out err exit] (docker/start-env name env host)]
             (is (= 0 (<! exit)))))
         (testing "env status"
           (let [[out err exit] (docker/env-status name env host)
                 sout (str/join "\n" (<! (a/into [] out)))]
             (is (= 0 (<! exit)))
             (is (re-find #"test\d*_nginx_.*Up" sout))))
         (testing "contact env"
           (let [req (node->async request (str "http://" docker-host ":48080"))
                 [err resp body] (<! req)]
             (is (nil? err))
             (is (= (.-statusCode resp) 200))))
         (testing "stop env"
           (let [[out err exit] (docker/stop-env name env host)]
             (is (= 0 (<! exit)))))
         (testing "env status"
           (let [[out err exit] (docker/env-status name env host)
                 sout (str/join "\n" (<! (a/into [] out)))]
             (is (= 0 (<! exit)))
             (is (re-find #"test\d*_nginx_.*Exit 0" sout))))
         (done)))))
