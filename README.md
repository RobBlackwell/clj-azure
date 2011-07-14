# clj-azure

A POC Windows Azure SDK for Clojure developers.

A work in progress. The core authentication works and there is a proof of concept for listing blobs and containers.


	; SLIME 2011-07-03
	user> (require 'clj-azure.blobs)
	nil
	user> (in-ns 'clj-azure.blobs)
	#<Namespace clj-azure.blobs>
	clj-azure.blobs> (list-containers *devstore*)
	{:url http://127.0.0.1:10000/devstoreaccount1/?comp=list, :method :get, :headers {Authorization SharedKey devstoreaccount1:N1HnyaWpkMXsuQkw2Lur6nQ8IeSHD9ILux8n7mmq9us=, x-ms-version 2009-09-19, x-ms-date Thu, 14 Jul 2011 15:26:33 GMT}}
	()
	clj-azure.blobs> (list-containers *devstore*)
	{:url http://127.0.0.1:10000/devstoreaccount1/?comp=list, :method :get, :headers {Authorization SharedKey devstoreaccount1:cPNKNKaSnLiAD+p59fRWTCyN4bLj63gpqmAiC/Ur53c=, x-ms-version 2009-09-19, x-ms-date Thu, 14 Jul 2011 15:27:46 GMT}}
	("foo")
	clj-azure.blobs> 

