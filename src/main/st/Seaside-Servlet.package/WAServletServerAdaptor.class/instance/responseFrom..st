converting
responseFrom: aRequestContext
	| seasideResponse nativeRequest contents |
	seasideResponse := aRequestContext response.
	nativeRequest := aRequestContext nativeRequest.

	"status"
	nativeRequest setResponseStatus: seasideResponse status _: seasideResponse message.

	"header"
	seasideResponse headers keysAndValuesDo: [ :key :value |
		nativeRequest addHeader: key _: value greaseString ].
	
	"cookies"
	seasideResponse cookies do: [ :seasideCookie |
		| servletCookie |
		servletCookie := self toServletCookie: seasideCookie fromRequest: nativeRequest.
		nativeRequest addCookie: servletCookie ].
	
	"body"
	contents := seasideResponse contents.
	contents isString
		ifTrue: [ nativeRequest setResponseContentsAsString: contents ]
		ifFalse: [ nativeRequest setResponseContentsAsByteArray: contents ]
	