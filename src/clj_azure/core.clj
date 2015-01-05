;;;; clj-azure.core
;;;; Copyright (c) 2011, Rob Blackwell.  All rights reserved.

(ns clj-azure.core
  "A Microsoft Azure SDK for Clojure developers."
  (:require [clojure.xml] 
            [clojure.string :as str])
  (:use [clojure.string :only [lower-case upper-case]]))

(defstruct account
  :account-name
  :account-key
  :blob-storage-url
  :table-storage-url
  :queue-storage-url)

(def dev-store-account
  (struct account
          "devstoreaccount1"
          "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="
          "http://127.0.0.1:10000/devstoreaccount1"
          "http://127.0.0.1:10002/devstoreaccount1"
          "http://127.0.0.1:10001/devstoreaccount1"))

(def sample-account
  (struct account
          "YOURACCOUNTNAME"
          "YOURACCOUNTKEY"
          "https://YOURACCOUNTNAME.blob.core.windows.net"
          "https://YOURACCOUNTNAME.table.core.windows.net"
          "https://YOURACCOUNTNAME.queue.core.windows.net"))

(defn parse-account 
  "Takes a connection string and returns an account struct"
  ([account-string]
    
    (letfn [
      (split-part [part] 
        (let [kv (str/split part #"=")]
          {(keyword (get kv 0)) (get kv 1)}))

      (get-protocol [parts]
        (if (contains? parts :DefaultEndpointsProtocol) (:DefaultEndpointsProtocol parts) "https"))

      (to-azure-account [parts]
        (struct account 
          (:AccountName parts)
          (:AccountKey parts)
          (str (get-protocol parts) "://" (:AccountName parts) ".blob.core.windows.net")
          (str (get-protocol parts) "://" (:AccountName parts) ".table.core.windows.net")
          (str (get-protocol parts) "://" (:AccountName parts) ".queue.core.windows.net")))
      ]
      (let [parts (into {} (map split-part (str/split account-string #";")))]
        (if (= (:UseDevelopmentStorage parts) "true") dev-store-account (to-azure-account parts))))
    )
  ([account-name account-key] 
    (struct account 
      account-name
      account-key
      (str "https://" account-name ".blob.core.windows.net")
      (str "https://" account-name ".table.core.windows.net")
      (str "https://" account-name ".queue.core.windows.net")))
)


(def x-ms-version "2011-08-18")

(defn now
  "Gets the current date and time in RFC1123 format e.g. Sun, 05 Jun 2008 21:15:07 GMT"
  []
  (let [f (java.text.SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss z" )]
    (do
      (.setTimeZone f (java.util.TimeZone/getTimeZone "GMT"))
      (.format f  (.getTime (java.util.Calendar/getInstance))))))

(defn canonicalized-headers
  "See http://msdn.microsoft.com/en-us/library/dd179428.aspx, Constructing the Canonicalized Headers String."
  [request]
  (let [headers (:headers request)]
    (apply str
           (map #(format "%s:%s\n" (clojure.string/lower-case (name %1)) (get headers %1))
                (sort (filter #(.startsWith (name %1) "x-ms-") (keys headers)))))))

(defn query-to-map [req]
  "Returns a url query string as a map of parameters and values."
  (if req
    (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" req)]
	       [(keyword k) v]))
    {}))

(defn canonicalized-resource-1
  "See http://msdn.microsoft.com/en-us/library/dd179428.aspx, 2009-09-19 Shared Key Format."
  [account-name url-string]
  (let [url (java.net.URL. url-string)
        uri-path (.getPath url)
        query-map (query-to-map (.getQuery url))]
    (str "/" account-name uri-path
         (apply str
           (map #(format "\n%s:%s" (clojure.string/lower-case (name %1)) (get query-map %1))
                (sort (keys query-map)))))))

(defn string-to-sign-1
  "See http://msdn.microsoft.com/en-us/library/dd179428.aspx, Blob and Queue Services (Shared Key Authentication)"
  [account-name request]
  (let [verb (clojure.string/upper-case (name (:method request)))
        headers (:headers request)
        url (:url request)]
    (str verb "\n"
         (headers "Content-Encoding") "\n"
         (headers "Content-Language") "\n"
         (headers "Content-Length") "\n"
         (headers "Content-MD5") "\n"
         (headers "Content-Type") "\n"
         (headers "Date") "\n"
         (headers "If-Modified-Since") "\n"
         (headers "If-Match") "\n"
         (headers "If-None-Match") "\n"
         (headers "If-Unmodified-Since") "\n"
         (headers "range") "\n"
         (canonicalized-headers request)
         (canonicalized-resource-1 account-name url))))

(defn hmac-string
  "Returns the SHA256 Hashed Message Authentication Code of the given string using the given key.
key is expressed as a Base64 encoded string."
  [key s]
  (let [k (org.apache.commons.codec.binary.Base64/decodeBase64 key)
        mac (javax.crypto.Mac/getInstance "hmacSHA256")
        secret (javax.crypto.spec.SecretKeySpec. k "hmacSHA256")]
    (do
      (.init mac secret)
      (String.
       (org.apache.commons.codec.binary.Base64/encodeBase64
        (.doFinal mac (.getBytes s)))))))


(defn sign
  "Returns a new request map with an Authorization header added."
  [account request]
  (let [s (string-to-sign-1 (:account-name account) request)]
    (merge {:headers (conj (:headers request)
			   {"Authorization"
			    (str "SharedKey " (:account-name account) ":"
				 (hmac-string (:account-key account) s)) })}
	   
	   (dissoc request :headers))))


(defn remove-header
  "Removes the named header from a request"
  [req header]
  (merge {:headers (dissoc (:headers req) header) }
	 (dissoc req :headers)))

(defn add-headers
  "Adds the specified headers to a request"
  [req headers]
  (merge {:headers (merge (:headers req) headers)}
	 (dissoc req :headers)))

(defn get-named-elements [xml]
  (for [elt (xml-seq (clojure.xml/parse (java.io.ByteArrayInputStream. (.getBytes xml "UTF-8")))) :when (= :Name (:tag elt))] (first (:content elt))))

