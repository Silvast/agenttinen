(ns agenttinen.opencode
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))


(def client-config
  {:throw-exceptions false
   :socket-timeout 60000
   :conn-timeout 5000})


(defn parse-json [s]
  (json/parse-string s true))


(defn to-json [m]
  (json/generate-string m))


(defn chat-completion [config api-key system-prompt user-prompt model]
  (let [url (get-in config [:opencode :api-url])
        request-body (json/generate-string
              {:model (or model (get-in config [:opencode :model]))
               :messages [{:role "system" :content system-prompt}
                          {:role "user" :content user-prompt}]
               :max_tokens (get-in config [:opencode :max-tokens])})]
    (try
      (let [response (http/post url
                                (merge client-config
                                       {:headers {"Content-Type" "application/json"
                                                  "Authorization" (str "Bearer " api-key)}
                                        :body request-body}))]
        (if (= 200 (:status response))
          (let [response-body (json/parse-string (:body response) true)
                content (get-in response-body [:choices 0 :message :content])]
            {:ok true :content content})
          {:ok false :error (str "Status " (:status response) ": " (:body response))}))
      (catch java.net.SocketTimeoutException e
        {:ok false :error (str "API request timed out: " (.getMessage e))})
      (catch Exception e
        {:ok false :error (str "API request failed: " (.getMessage e))}))))
