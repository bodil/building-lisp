(ns repl.core
  (:require [org.httpkit.server :as s :refer [send! websocket? with-channel
                                              on-receive on-close]]
            [cheshire.core :refer [generate-string parse-string]]
            [cheshire.generate :refer [add-encoder]]
            [clojure.edn :as edn])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(add-encoder clojure.lang.Namespace
             (fn [ns gen]
               (.writeString gen (str (ns-name ns)))))

(defn stream [s]
  (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. s)))

(defn eval-form [ns form]
  (with-bindings {#'*ns* ns}
    (try
      (let [result (eval form)]
        {:result (and result (pr-str result)) :ns *ns*})
      (catch Throwable e
        {:error (.getMessage e) :ns *ns*}))))

(defn read-next [s]
  (try (edn/read {:eof :stream-eof} s)
       (catch Exception e {:error (.getMessage (.getCause e))})))

(defn eval-stream [ns s]
  (println "hai eval-stream")
  (loop [form (read-next s)
         ns ns
         results []]
    (println "next in stream:" form)
    (if-not (= :stream-eof form)
      (if (and (map? form) (:error form))
        (conj results (assoc form :line (.getLineNumber s)))
        (let [result (assoc (eval-form ns form) :line (.getLineNumber s))]
          (if (result :error)
            (conj results result)
            (recur (read-next s)
                   (:ns result)
                   (conj results result)))))
      results)))

(defn process-request [ns channel data]
  (println "received data on socket: " data)
  (try
    (let [data (parse-string data)
          code (data "eval")
          type (data "type")
          id (data "messageId")]
      (if (and code (= "text/x-clojure" type))
        ;; future
        (let [result (eval-stream (create-ns 'user) (stream code))
              _ (println "eval result:" result)
              out (generate-string {:code code :result result
                                    :messageId id})]
          (println "sending:" out)
          (send! channel out))
        (send! channel (generate-string {:error "No Clojure code to evaluate."
                                         :messageId id}))))
    (catch JsonParseException e
      (send! channel (generate-string {:error "Bad JSON, try again."})))))

(defn process-socket [channel]
  (on-receive channel (partial process-request (create-ns 'user) channel)))

(defn handler [req]
  (with-channel req channel
    (if (websocket? channel)
      (process-socket channel)
      (send! channel {:status 404
                      :headers {"Content-Type" "text/plain"}
                      :body "Nothing here. You might try a WebSocket."}))))

(defonce server (atom nil))

(defn start []
  (reset! server (s/run-server #'handler {:port 31336})))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (println "REPL server listening.")
  (start))
