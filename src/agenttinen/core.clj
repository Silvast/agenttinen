(ns agenttinen.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
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
  (println "Starting Agenttinen server on port" (or (System/getenv "PORT") (get-in config [:server :port]) 3000))
  (let [config-with-prompt (assoc config :system-prompt system-prompt)]
    (server/start-server config-with-prompt)))