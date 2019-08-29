converting-request
requestHeadersFor: aNativeRequest
	| headers |
	headers := GROrderedMultiMap new.

	self list: aNativeRequest getRequestHeaders do: [ :entry |
		| headerName |
		headerName := entry getKey.
		self list: entry getValue do: [ :headerValue |
			headers at: headerName add: headerValue ] ].
	^ headers