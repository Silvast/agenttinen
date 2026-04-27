(ns agenttinen.opencode
  (:require [cheshire.core :as json]
            [clj-http.client :as http])
  (:gen-class))


(def client-config
  {:headers {"Content-Type" "application/json"}
   :throw-exceptions false
   :socket-timeout 10000
   :conn-timeout 5000})


(defn parse-json [s]
  (json/parse-string s true))


(defn to-json [m]
  (json/generate-string m))


(defn chat-completion [config api-key system-prompt user-prompt]
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