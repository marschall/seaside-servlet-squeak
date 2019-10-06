instance creation
onContextPath: aString requestHandler: aRequestHandler
	^ self basicNew
		initializeOnContextPath: (GRPlatform subStringsIn: aString splitBy: $/)
		requestHandler: aRequestHandler