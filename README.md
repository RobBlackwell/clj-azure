# clj-azure

A proof-of-concept Windows Azure SDK for Clojure developers.

DO NOT USE yet.

A work in progress. The core authentication works and there is a proof
of concept for listing blobs and containers.

Now also shows how to work with Windows Azure Media Services (using
OAuth token from ACS).

Contributions welcome.

Rob Blackwell

September 2012


## Getting started

Import in the repl:

```clojure
(use `[clj-azure.core] `[clj-azure.blobs])
```

...or in your program:

```clojure
(ns my-app.core
  (:use [clj-azure.core])
  (:use	[clj-azure.blobs])
)
```

Then parse create a storage account:

```clojure
;;; using storage account name and key
(parse-account "ACCOUNT_NAME" "ACCOUNT_KEY")

;;; ...or using a storage connection string
(parse-account "AccountName=ACCOUNT_NAME;AccountKey=ACCOUNT_KEY")
```

## Blobs

```clojure
(list-containers account)
```