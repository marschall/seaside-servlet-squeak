converting-request
requestCookiesFor: aNativeRequest
	^ self list: aNativeRequest getCookies collect: [ :each |
		WARequestCookie
			key: each getName
			value: each getValue ]