converting-request
requestCookiesFor: aNativeRequest
	^ WAJavaSupport list: aNativeRequest getCookies collect: [ :each |
		WARequestCookie
			key: each getName
			value: each getValue ]