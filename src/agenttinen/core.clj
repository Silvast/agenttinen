(ns agenttinen.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [agenttinen.server :as server])
  (:gen-class))


(def ^:private default-config-file "configuration/default.edn")


(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))


(def config
  (load-config default-config-file))


(def system-prompt
  (slurp "resources/system_prompt.md"))


(defn -main []
  (println "Starting Agenttinen server on port" (or (env :port) (get-in config [:server :port]) 3000))
  (let [config-with-prompt (assoc config :system-prompt system-prompt)]
    (server/start-server config-with-prompt)))
