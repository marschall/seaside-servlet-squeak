converting
contextFor: aNativeRequest
	^ (super contextFor: aNativeRequest)
		nativeRequest: aNativeRequest;
		yourself