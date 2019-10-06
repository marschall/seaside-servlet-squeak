converting-request
requestUrlFor: aNativeRequest
	^(WAUrl absolute: aNativeRequest getQueryStringRaw)
		decodedWith: self codec