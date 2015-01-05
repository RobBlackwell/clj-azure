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

(deftest test-list-containers
  (is (= ((list-containers-raw sample-account) :status) 200)))


