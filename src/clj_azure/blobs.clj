(ns clj-azure.blobs
  "Access to Windows Azure Blob Storage"
  (:require clj-http.client)
  (:use clj-azure.core))

(defn my-wrap-request
  "Returns a custom HTTP request function constructed from clj-http"
  [request]
  (-> request
    clj-http.client/wrap-redirects
    ;;wrap-exceptions
    clj-http.client/wrap-decompression
    clj-http.client/wrap-input-coercion
    clj-http.client/wrap-output-coercion
    clj-http.client/wrap-query-params
    clj-http.client/wrap-basic-auth
    clj-http.client/wrap-user-info
    clj-http.client/wrap-accept
    clj-http.client/wrap-accept-encoding
    clj-http.client/wrap-content-type
    clj-http.client/wrap-method
    clj-http.client/wrap-url))

(def myrequest
  (my-wrap-request #'clj-http.core/request))

(defn myprintln [x]
  (do
    (println x)
    x))

(defn get-named-elements [xml]
  (for [elt (xml-seq (clojure.xml/parse (java.io.ByteArrayInputStream. (.getBytes xml "UTF-8")))) :when (= :Name (:tag elt))] (first (:content elt))))

(defn blob-storage-request [method url account headers]
  (myrequest
   (myprintln (sign account {:method method :url url :headers (conj headers {"x-ms-date" (now) "x-ms-version" "2009-09-19"})}))))

(defn list-containers-raw [account]
  (blob-storage-request
   :get
   (format "%s/?comp=list" (:blob-storage-url account)) account {}))

(defn list-containers [account]
  (get-named-elements (:body (list-containers-raw account))))


(defn list-blobs-raw [account container]
  (blob-storage-request
   :get
   (format "%s/%s?restype=container&comp=list" (:blob-storage-url account) container) account {}))


(defn list-blobs [account container]
  (get-named-elements (:body (list-blobs-raw account container))))




