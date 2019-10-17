converting-request
requestFieldsFor: aNativeRequest
	| fields |
	fields := WARequestFields new.


	WAJavaSupport list: aNativeRequest getRequestFields do: [ :entry |
		| fieldName |
		fieldName := entry getKey.
		WAJavaSupport list: entry getValue do: [ :fieldValue |
			fields at: fieldName add: fieldValue ] ].

	WAJavaSupport list: aNativeRequest getFormParts do: [ :each |
		| part |
		part := each isFile
			ifTrue: [ self toWaFile: each ]
			ifFalse: [ each getValue ].
		
		fields at: each getPartName add: part ].

	^ fields