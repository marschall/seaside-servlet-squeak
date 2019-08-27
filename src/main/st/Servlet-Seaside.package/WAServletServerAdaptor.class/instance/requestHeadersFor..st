converting-request
requestHeadersFor: aNativeRequest
	| headers |
	headers := GROrderedMultiMap new.
	aNativeRequest getRequestHeaders do: [ :entry |
		| headerName |
		headerName := entry getKey.
		entry getValue do: [ :headerValue |
			headers at: headerName add: headerValue ] ].
	^ headers