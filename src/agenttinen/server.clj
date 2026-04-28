(ns agenttinen.server
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [agenttinen.opencode :as opencode]))


(defn chat-handler [config request]
  (let [api-key (or (env :opencode-api-key)
                    (get-in config [:opencode :api-key]))]
    (if-not api-key
      {:status 401
       :headers {"Content-Type" "application/json"}
       :body (opencode/to-json {:error "OPENCODE_API_KEY not set - set in environment (OPENCODE_API_KEY), .edn config, or as :opencode-api-key"})}
      (let [body-str (slurp (:body request))
            body (opencode/parse-json body-str)
            user-prompt (:user-prompt body)]
        (if-not user-prompt
          {:status 400
           :headers {"Content-Type" "application/json"}
           :body (opencode/to-json {:error "Missing user-prompt in request body"})}
          (let [system-prompt (get-in config [:system-prompt])
                result (opencode/chat-completion config api-key system-prompt user-prompt)]
            (if (:ok result)
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body (opencode/to-json {:content (:content result)})}
              {:status 500
               :headers {"Content-Type" "application/json"}
               :body (opencode/to-json {:error (:error result)})})))))))


(defn root-handler [_]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Agenttinen API running. Try POST /api/chat"})


(defn app [config]
  (ring/ring-handler
    (ring/router
      [["/" {:get root-handler}]
       ["/api/chat" {:post (partial chat-handler config)}]])
    (ring/create-default-handler)))


(defn start-server [config]
  (let [port (or (env :port) (get-in config [:server :port]) 3000)]
    (jetty/run-jetty (app config) {:port port :join? true})))
