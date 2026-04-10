(ns gimel.handler)

(defn wrap-html-content-type [handler]
  (fn [request]
    (let [response (handler request)]
      (if (.endsWith (:uri request) ".html")
        (assoc-in response [:headers "Content-Type"] "text/html")
        response))))
