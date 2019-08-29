converting-request
requestFieldsFor: aNativeRequest
	| headers |
	headers := GROrderedMultiMap new.


	self list: aNativeRequest getRequestFields do: [ :entry |
		| fieldName |
		fieldName := entry getKey.
		self list: entry getValue do: [ :fieldValue |
			headers at: fieldName add: fieldValue ] ].

	self list: aNativeRequest getServletFiles do: [ :each |
		| file |
		file := WAFile new
			fileName: each getFileName;
			contentType: (WAMimeType fromString: each getContentType);
			contents: each getContents;
			yourself.
		headers at: each getFileName add: file ].

	^ headers