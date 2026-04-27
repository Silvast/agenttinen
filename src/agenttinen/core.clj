(ns agenttinen.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty])
  (:gen-class))


(def ^:private default-config-file "configuration/default.edn")


(defn- load-config [file]
  (with-open [reader (io/reader file)]
    (edn/read (java.io.PushbackReader. reader))))


(def config
  (load-config default-config-file))


(def system-prompt
  (slurp "resources/system_prompt.md"))


(def client-config
  {:headers {"Content-Type" "application/json"}
   :throw-exceptions false
   :socket-timeout 10000
   :conn-timeout 5000})


(defn chat-completion [api-key system-prompt user-prompt]
  (let [url (get-in config [:opencode :api-url])
        request-body (json/generate-string
              {:model (get-in config [:opencode :model])
               :messages [{:role "system" :content system-prompt}
                          {:role "user" :content user-prompt}]
               :max_tokens (get-in config [:opencode :max-tokens])})
        response (http/post url
                            (merge client-config
                                   {:headers {"Authorization" (str "Bearer " api-key)}
                                    :body request-body}))]
  (if (= 200 (:status response))
    (let [response-body (json/parse-string (:body response) true)
          content (get-in response-body [:choices 0 :message :content])]
      {:ok true :content content})
    {:ok false :error (str "Status " (:status response) ": " (:body response))})))


(defn parse-json [s]
  (json/parse-string s true))


(defn to-json [m]
  (json/generate-string m))


(defn chat-handler [request]
  (let [api-key (or (env :opencode-api-key)
                    (System/getenv "OPENCODE_API_KEY")
                    (get-in config [:opencode :api-key]))]
    (if-not api-key
      {:status 401
       :headers {"Content-Type" "application/json"}
       :body (to-json {:error "OPENCODE_API_KEY not set - set in environment (OPENCODE_API_KEY), .edn config, or as :opencode-api-key"})}
      (let [body-str (slurp (:body request))
            body (parse-json body-str)
            user-prompt (:user-prompt body)]
        (if-not user-prompt
          {:status 400
           :headers {"Content-Type" "application/json"}
           :body (to-json {:error "Missing user-prompt in request body"})}
          (let [result (chat-completion api-key system-prompt user-prompt)]
            (if (:ok result)
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body (to-json {:content (:content result)})}
              {:status 500
               :headers {"Content-Type" "application/json"}
               :body (to-json {:error (:error result)})})))))))


(defn root-handler [_]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Agenttinen API running. Try POST /api/chat"})

(def app
  (ring/ring-handler
    (ring/router
      [["/" {:get root-handler}]
       ["/api/chat" {:post chat-handler}]])
    (ring/create-default-handler)))


(defn start-server []
  (let [port (or (env :port) (get-in config [:server :port]) 3000)]
    (jetty/run-jetty app {:port port :join? true})))


(defn -main []
  (let [port (or (env :port) (get-in config [:server :port]) 3000)]
    (println "Starting Agenttinen server on port" port)
    (start-server)))