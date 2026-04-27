(ns agenttinen.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [agenttinen.core :refer [app]]))

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
