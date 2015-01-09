(ns clj-azure.test.core
  (:use [clj-azure.core])
  (:use [clj-azure.blobs])
  (:use [clojure.test]))

(deftest test-hmac-string
  (let
      [key "GuGbCQ41a9G1vtS1/dairlSMbXhHVzoA8+VPrbWxtj94o0aoAQdsgaaoYQASWqG9mj8xDvP1hSkvSVcLC34CfA=="
       test "Hello World"
       result "+UTfogPQ1ELBA4l+A7LwT1lbZVbP34F/CQzXaXqwfWA="]

    (is (= (hmac-string key test) result))))


(deftest test-canonicalized-headers
  (let
      [request { :url "http://robblackwell.blob.core.windows.net/?comp=list&restype=container"
                :headers { :x-ms-version, "2009-09-19" :x-ms-date "Sun, 12 Jun 2011 10:00:45 GMT"}
                :method :get}
       result "x-ms-date:Sun, 12 Jun 2011 10:00:45 GMT\nx-ms-version:2009-09-19\n"]
    (is (= (canonicalized-headers request) result))))


(deftest test-canonicalized-resource-1
  (let
      [uri "https://myaccount.blob.core.windows.net/mycontainer?restype=container&comp=metadata"
       result "/myaccount/mycontainer\ncomp:metadata\nrestype:container"]

    (is (= (canonicalized-resource-1 "myaccount" uri) result))))


(deftest test-string-to-sign-1
  (let
      [request { :url "http://robblackwell.blob.core.windows.net/?comp=list&restype=container"
                :headers { :x-ms-version, "2009-09-19" :x-ms-date "Sun, 12 Jun 2011 10:00:45 GMT"}
                :method :get}
       result "GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Sun, 12 Jun 2011 10:00:45 GMT\nx-ms-version:2009-09-19\n/myaccount/\ncomp:list\nrestype:container"]

    (is (= (string-to-sign-1 "myaccount" request) result))))


(deftest test-parse-account 
  (let [acct (parse-account "DefaultEndpointsProtocol=https;AccountName=two10ra;AccountKey=THISISTHEACCOUNTKEY")]
    (is (= (:account-name acct) "two10ra"))
    (is (= (:account-key acct) "THISISTHEACCOUNTKEY"))
    (is (= (:blob-storage-url acct) "https://two10ra.blob.core.windows.net"))))

(deftest test-parse-account-with-http-endpoint
  (let [acct (parse-account "DefaultEndpointsProtocol=http;AccountName=two10ra;AccountKey=THISISTHEACCOUNTKEY")]
    (is (= (:account-name acct) "two10ra"))
    (is (= (:account-key acct) "THISISTHEACCOUNTKEY"))
    (is (= (:blob-storage-url acct) "http://two10ra.blob.core.windows.net"))))

(deftest test-parse-emulator-account 
  (let [acct (parse-account "UseDevelopmentStorage=true")]
    (is (= (:account-name acct) "devstoreaccount1"))))

(deftest test-account-name-and-key
  (let [acct (parse-account "two10ra" "THISISTHEACCOUNTKEY")]
    (is (= (:account-name acct) "two10ra"))
    (is (= (:account-key acct) "THISISTHEACCOUNTKEY"))
    (is (= (:blob-storage-url acct) "https://two10ra.blob.core.windows.net"))))
  

(deftest test-list-containers
  (is (= ((list-containers-raw dev-store-account) :status) 200)))

(deftest test-create-container
  (is (= (create-container dev-store-account "foobarbaz") true)))

;;; containers which have already been created should return false (unsuccessful)
(deftest test-create-container
  (is (= (create-container dev-store-account "foobarbaz") false)))

(deftest test-put-blob
  (put-blob dev-store-account "foobarbaz" "test.txt" "Hello World"))

(deftest test-get-blob
  (is (= (:content (get-blob dev-store-account "foobarbaz" "test.txt")) "Hello World")))

(deftest test-get-blob-properties
  (is (= 
    (:x-ms-lease-status 
      (get-blob-properties dev-store-account "foobarbaz" "test.txt")) "unlocked")))

(deftest test-del-blob
  (is (= (del-blob dev-store-account "foobarbaz" "test.txt") true )))


