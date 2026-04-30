(ns agenttinen.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [agenttinen.opencode :as opencode]
            [agenttinen.server :as server]))


(def test-config
  {:system-prompt "You are helpful."
   :opencode {:api-url "https://example.test/chat"
              :api-key "test-api-key"
              :model "minimax-m2.5-free"
              :allowed-models ["minimax-m2.5-free" "test-model"]
              :max-tokens 1000}})


(defn app [request]
  ((server/app test-config) request))

(deftest health-check
  (testing "GET / returns 200"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response))))))

(deftest unknown-route-returns-404
  (testing "unregistered path returns 404"
    (let [response (app (mock/request :get "/does-not-exist"))]
      (is (= 404 (:status response))))))

(deftest wrong-method-on-root-returns-405
  (testing "POST on / returns 405 method not allowed"
    (let [response (app (mock/request :post "/"))]
      (is (= 405 (:status response))))))

(deftest wrong-method-on-chat-returns-405
  (testing "GET on /api/chat returns 405 method not allowed"
    (let [response (app (mock/request :get "/api/chat"))]
      (is (= 405 (:status response))))))

(deftest chat-missing-user-prompt
  (testing "POST /api/chat without user-prompt returns 400"
    (let [response (app (-> (mock/request :post "/api/chat")
                            (mock/content-type "application/json")
                            (mock/body (json/generate-string {:other "field"}))))]
      (is (= 400 (:status response))))))


(deftest chat-uses-requested-allowed-model
  (testing "POST /api/chat forwards a request model only when it is allowed"
    (let [captured-model (atom nil)]
      (with-redefs [opencode/chat-completion (fn [_ _ _ _ model]
                                               (reset! captured-model model)
                                               {:ok true :content "reply"})]
        (let [response (app (-> (mock/request :post "/api/chat")
                                (mock/content-type "application/json")
                                (mock/body (json/generate-string {:user-prompt "Hello"
                                                                  :model "test-model"}))))]
          (is (= 200 (:status response)))
          (is (= "test-model" @captured-model)))))))


(deftest chat-uses-default-model-when-request-omits-model
  (testing "POST /api/chat uses the configured default model when request omits model"
    (let [captured-model (atom nil)]
      (with-redefs [opencode/chat-completion (fn [_ _ _ _ model]
                                               (reset! captured-model model)
                                               {:ok true :content "reply"})]
        (let [response (app (-> (mock/request :post "/api/chat")
                                (mock/content-type "application/json")
                                (mock/body (json/generate-string {:user-prompt "Hello"}))))]
          (is (= 200 (:status response)))
          (is (= "minimax-m2.5-free" @captured-model)))))))


(deftest chat-rejects-unsupported-request-model
  (testing "POST /api/chat rejects request models outside the configured allowlist"
    (let [called? (atom false)]
      (with-redefs [opencode/chat-completion (fn [& _]
                                               (reset! called? true)
                                               {:ok true :content "reply"})]
        (let [response (app (-> (mock/request :post "/api/chat")
                                (mock/content-type "application/json")
                                (mock/body (json/generate-string {:user-prompt "Hello"
                                                                  :model "expensive-model"}))))
              body (json/parse-string (:body response) true)]
          (is (= 400 (:status response)))
          (is (= "Unsupported model" (:error body)))
          (is (false? @called?)))))))


(deftest chat-completion-sends-selected-model-upstream
  (testing "chat-completion includes the resolved model in the upstream request body"
    (let [captured-body (atom nil)]
      (with-redefs [http/post (fn [_ request]
                                (reset! captured-body (:body request))
                                {:status 200
                                 :body (json/generate-string
                                         {:choices [{:message {:content "reply"}}]})})]
        (let [result (opencode/chat-completion test-config
                                               "api-key"
                                               "System prompt"
                                               "User prompt"
                                               "test-model")]
          (is (= {:ok true :content "reply"} result))
          (is (= "test-model" (:model (json/parse-string @captured-body true)))))))))
