accessing
urlFor: aRequestHandler
	aRequestHandler == requestHandler
		ifFalse: [ WAUnregisteredHandlerError signal ].
	^ self url
		addAllToPath: contextPath;
		yourself