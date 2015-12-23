(ns denvr.docker-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :refer [<!] :as a]
            [denvr.util :refer-macros [env]]
            [denvr.docker.core :as docker]
            [denvr.cli :as cli]
            [denvr.config.core :as cfg])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def configdir (str (env "HOME") "/.denvr"))
(def host (cfg/read-host configdir))
(def env
  {:containers [{:id "nginx"
                 :image "nginx:1.9.8"
                 :ports [[80 :tcp]]}]})
(deftest start-status-stop-test
  (async
   done
   (go (let [name (str (gensym "test"))
             id' (atom nil)]
         (testing "validate env"
           (is (= env (cfg/check-env env))))
         (testing "start env"
           (let [res (<! (a/into #{} (docker/start-env name env host)))]
             (is (= 1 (count res)))
             (is (some? (get (first res) "Id")))
             (is (= "nginx:1.9.8" (get (first res) "Image")))
             (is (= name (get-in (first res) ["Config" "Labels" "denvr"])))
             (is (= 80 (get-in (first res) ["Ports" 0 "PrivatePort"])))
             (reset! id' (get (first res) "Id"))))
         (testing "env status"
           (let [res (<! (a/into #{} (docker/env-status name env host)))]
             (is (= 1 (count res)))
             (is (= @id' (get (first res) "Id")))))
         (testing "stop env"
           (let [res (<! (a/into #{} (docker/stop-env name env host)))]
             (is (= 1 (count res)))
             (is (= (str "Stopped " @id') (first res)))))
         (testing "env status"
           (let [res (<! (a/into #{} (docker/env-status name env host)))]
             (is (= #{} res))))
         (done)))))
