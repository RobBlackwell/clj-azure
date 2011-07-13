(ns clj-azure.blobs
  "Access to Windows Azure Blob Storage"
  (:use clj-azure.core clj-http.client))

(defn my-wrap-request
  "Returns a custom HTTP request function constructed from clj-http"
  [request]
  (-> request
    wrap-redirects
    ;;wrap-exceptions
    wrap-decompression
    wrap-input-coercion
    wrap-output-coercion
    wrap-query-params
    wrap-basic-auth
    wrap-user-info
    wrap-accept
    wrap-accept-encoding
    wrap-content-type
    wrap-method
    wrap-url))

(def myrequest
  (my-wrap-request #'clj-http.core/request))

(defn myprintln [x]
  (do
    (println x)
    x))

(defn blob-storage-request [method url account headers]
  (myrequest
   (myprintln (sign account {:method method :url url :headers (conj headers {"x-ms-date" (now) "x-ms-version" "2009-09-19"})}))))

(defn list-containers [account]
  (blob-storage-request :get
   (format "%s/?comp=list" (:blob-storage-url account)) account {}))

