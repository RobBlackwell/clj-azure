;;;; azure.media
;;;; Copyright (c) 2012, Rob Blackwell.  All rights reserved.

(ns azure.media
  "Access to Windows Azure Media Services"
  (:require clj-http.client))

(defn get-token [client-id client-secret & [options]]
  (:access_token 
   (:body 
    (clj-http.client/request
     (merge
      {:method :post 
       :url "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13"
       :headers {"Content-Type" "application/x-www-form-urlencoded", 
                 "Expect" "100-continue", 
                 "Connection" "Keep-Alive" 
                 "Host" "wamsprodglobal001acs.accesscontrol.windows.net"}
       :form-params {:grant_type "client_credentials",
                     :client_id client-id,
                     :client_secret client-secret
                     :scope "urn:WindowsAzureMediaServices"}
       :as :json}
      options)))))


;; Beware this might move - check your HTTP redirects!
(def root-uri "https://wamsdubclus001rest-hs.cloudapp.net/api/")

(defn media-service-get [token path & [options]]
  (clj-http.client/request
   (merge
    {:method :get 
     :url (str root-uri path)
     :headers {"Content-Type" "application/json;odata=verbose", 
               "Accept" "application/json;odata=verbose" ,
               "DataServiceVersion" "3.0",
               "MaxDataServiceVersion" "3.0",
               "x-ms-version" "1.0",
               "Authorization" (format "Bearer %s" token) 
               }
     :follow-redirects false
     :as :json }
    options)))

(defn get-media-processors [token & options]
  (media-service-get token "MediaProcessors" options))

(defn get-assets [token & options]
  (media-service-get token "Assets" options))

(defn get-jobs [token & options]
  (media-service-get token "Jobs" options))




  