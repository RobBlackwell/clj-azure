(ns clj-azure.test.blob
  (:use [clj-azure.core])
  (:use [clj-azure.blobs])
  (:use [clojure.test]))


(deftest test-list-containers
  (is (= ((list-containers-raw dev-store-account) :status) 200)))

(deftest test-create-container
  (is (= (create-container dev-store-account "foobarbaz") true)))

;;; containers which have already been created should return false (unsuccessful)
(deftest test-create-container
  (is (= (create-container dev-store-account "foobarbaz") false)))

(deftest test-put-blob
  (put-blob dev-store-account "foobarbaz" "test.txt" "Hello World!"))

(deftest test-get-blob
  (is (= (:content (get-blob dev-store-account "foobarbaz" "test.txt")) "Hello World!")))

(deftest test-set-blob-properties
  (set-blob-properties dev-store-account "foobarbaz" "test.txt" {:x-ms-blob-content-encoding "text/html"}))

(comment
(deftest test-get-blob-properties
  (is (= 
    (:x-ms-blob-content-encoding 
      (get-blob-properties dev-store-account "foobarbaz" "test.txt")) "text/html")))
)

(deftest test-set-blob-metadata
  (set-blob-metadata dev-store-account "foobarbaz" "test.txt" {:foo "bar"}))


(deftest test-get-blob-metadata
  (get-blob-metadata dev-store-account "foobarbaz" "test.txt"))

(comment
(deftest test-del-blob
  (is (= (del-blob dev-store-account "foobarbaz" "test.txt") true )))
)

