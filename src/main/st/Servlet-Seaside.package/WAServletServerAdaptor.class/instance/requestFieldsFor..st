converting-request
requestFieldsFor: aNativeRequest
	| fields |
	fields := WARequestFields new.


	self list: aNativeRequest getRequestFields do: [ :entry |
		| fieldName |
		fieldName := entry getKey.
		self list: entry getValue do: [ :fieldValue |
			fields at: fieldName add: fieldValue ] ].

	self list: aNativeRequest getFormParts do: [ :each |
		| part |
		part := each isFile
			ifTrue: [
				WAFile new
					fileName: each getFileName;
					contentType: (WAMimeType fromString: each getContentType);
					contents: each getContents;
					yourself ]
			ifFalse: [ each getValue ].
		
		fields at: each getPartName add: part ].

	^ fields