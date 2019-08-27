converting-request
requestCookiesFor: aNativeRequest
	^ aNativeRequest getCookies collect: [ :each |
		WARequestCookie
			key: each getName
			value: each getValue ]