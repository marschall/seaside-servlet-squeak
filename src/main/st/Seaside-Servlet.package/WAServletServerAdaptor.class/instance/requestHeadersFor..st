converting-request
requestHeadersFor: aNativeRequest
	| headers |
	headers := GROrderedMultiMap new.

	WAJavaSupport list: aNativeRequest getRequestHeaders do: [ :entry |
		| headerName |
		headerName := entry getKey.
		WAJavaSupport list: entry getValue do: [ :headerValue |
			headers at: headerName add: headerValue ] ].
	^ headers