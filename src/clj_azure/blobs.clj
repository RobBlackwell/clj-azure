;;;; azure.blobs
;;;; Copyright (c) 2011, Rob Blackwell.  All rights reserved.

(ns clj-azure.blobs
  "Access to Windows Azure Blob Storage"
  (:require clj-http.lite.client)
  (:require clojure.xml)
  (:use clj-azure.core))

;; Unfortunately the underlying Apache HttpComponents library doesn't
;; allow you to set Content-Length explicitly. The Azure signature
;; requires it to be known in advance of making the request, so we add
;; it to the request and then remove it before making the call.

(defn blob-storage-request 
  "Makes an HTTP request to Windows Azure Blob store"
  [account req]
  (let [request (add-headers req {"x-ms-date" (now) "x-ms-version" x-ms-version })]
    (clj-http.lite.client/request 
     (remove-header (sign account request) "Content-Length"))))

(defn blob-storage-request-put
  "Makes an HTTP request to Windows Azure Blob store"
  [account req]
  (let [request (add-headers req {"x-ms-date" (now) "x-ms-version" x-ms-version })]
    (clj-http.lite.client/put (:url req) (remove-header (sign account request) "Content-Length"))))


;; Low Level REST API

(defn list-containers-raw
  "Lists all of the containers in the given storage account."
  [account]
  (blob-storage-request
   account
   {:method :get
    :url (format "%s/?comp=list" (:blob-storage-url account))}))

(defn set-blob-service-properties-raw
  "Sets the properties of the Blob service."
  [account]
  (throw (UnsupportedOperationException.)))

(defn get-blob-service-properties-raw
  "Gets the properties of the Blob service."
  [account]
  (blob-storage-request
   account
   {:method :get
    :url (format "%s/?restype=service&comp=properties" (:blob-storage-url account))}))

(defn create-container-raw
  "Creates a new container in the given storage account."
  [account container]
  (blob-storage-request-put
   account
   {:method :put
    :url (format "%s/%s?restype=container" (:blob-storage-url account) container)
    :headers {"Content-Length" "0"}
    :body ""}))

(defn get-container-properties-raw
  "Returns all properties and metadata on the container."
  [account container]
  (throw (UnsupportedOperationException.)))

(defn get-container-metadata-raw
  "Returns only user-defined metadata for the specified container."
  [account container]
  (throw (UnsupportedOperationException.)))

(defn set-container-metadata-raw
  "Sets metadata headers on the container."
  [account container]
  (throw (UnsupportedOperationException.)))

;; Get Container ACL (REST API)
;; Gets the access control list (ACL) and any container-level access policies for the container.

;; Set Container ACL (REST API)
;; Sets the ACL and any container-level access policies for the container.

(defn delete-container-raw
  "Deletes the container and any blobs that it contains."
  [account container-name]
  (blob-storage-request
   account
   {:method :delete
    :url (format "%s/%s?restype=container" (:blob-storage-url account) container-name)
    :headers {"Content-Length" "0"} }))

(defn list-blobs-raw
  "Lists all of the blobs in the given container."
  [account container]
  (blob-storage-request
   account
   {:method :get
    :url (format "%s/%s?restype=container&comp=list" (:blob-storage-url account) container)}))

(defn put-blob
  "Creates a new blob or replaces an existing blob within a container."
  [account container blob data]
  (blob-storage-request
   account
   {:method :put
    :url (format "%s/%s/%s" (:blob-storage-url account) container blob )
    :headers {"Content-Length" (str (count data)) "x-ms-blob-type" "BlockBlob"}
    :body data}))

(defn headers-to-map [response]
  (into {} (map (fn [kv] { (keyword (get kv 0) ) (get kv 1)}) (:headers response))))

(defn get-blob
  "Downloads a blob from a container"
  [account container blob]
  (let [response (blob-storage-request account {
    :method :get
    :url (format "%s/%s/%s" (:blob-storage-url account) container blob )})]
    {:content (:body response) :headers (headers-to-map response)}))


(defn get-blob-properties
  "Gets the properies for a blob"
  [account container blob]
  (headers-to-map (blob-storage-request account {
    :method :head
    :url (format "%s/%s/%s" (:blob-storage-url account) container blob )})))

(defn del-blob
  "Deletes a blob from a container"
  [account container blob]
  (= 202 (:status (blob-storage-request account {
    :method :delete
    :url (format "%s/%s/%s" (:blob-storage-url account) container blob )}))))


;; Get Blob (REST API)
;; Reads or downloads a blob from the system, including its metadata and properties.

;; Get Blob Properties (REST API)
;; Returns all properties and metadata on the blob.

;; Set Blob Properties (REST API)
;; Sets system properties defined for a blob.

;; Get Blob Metadata (REST API)
;; Retrieves metadata headers on the blob.

;; Set Blob Metadata (REST API)
;; Sets metadata headers on the blob.

;; Delete Blob (REST API)
;; Deletes a blob.

;; Lease Blob (REST API)
;; Establishes an exclusive one-minute write lock on a blob. To write to a locked blob, a client must provide a lease ID.

;; Snapshot Blob (REST API)
;; Creates a snapshot of a blob.

;; Copy Blob (REST API)
;; Copies a source blob to a destination blob within the same storage account.

;; Put Block (REST API)
;; Creates a new block to be committed as part of a block blob.

;; Put Block List (REST API)
;; Commits a blob by specifying the set of block IDs that comprise the block blob.

;; Get Block List (REST API)
;; Retrieves the list of blocks that make up the block blob.

;; Put Page (REST API)
;; Puts a range of pages into a page blob, or clears a range of pages from the blob.

;; Get Page Regions (REST API)
;;Returns a list of active page ranges for a page blob. Active page ranges are those that have been populated with data.

;; High Level API

(defn list-containers [account]
  (get-named-elements (:body (list-containers-raw account))))

(defn list-blobs [account container]
  (get-named-elements (:body (list-blobs-raw account container))))

;;;  we should probably just catch the exceptions for known status codes...
(defn create-container [account container-name]
  (try
    (create-container-raw account container-name)
    true
    (catch Exception e 
      false)))





