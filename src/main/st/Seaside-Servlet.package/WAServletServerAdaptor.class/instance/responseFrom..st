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
		servletCookie := nativeRequest newCookie: seasideCookie key _: seasideCookie value.
		servletCookie setVersion: seasideCookie version.
		seasideCookie comment ifNotNilDo: [ :comment | servletCookie setComment: comment ].
		seasideCookie domain ifNotNilDo: [ :domain | servletCookie setDomain: domain ].
		"GraalSqueak bug calling int methods"
		"servletCookie setMaxAge: seasideCookie maxAge."
		servletCookie setPath: seasideCookie path.
		servletCookie setSecure: seasideCookie secure.
		servletCookie setHttpOnly: seasideCookie httpOnly.
		nativeRequest addCookie: servletCookie ].
	
	"body"
	contents := seasideResponse contents.
	contents isString
		ifTrue: [ nativeRequest setResponseContentsAsString: contents ]
		ifFalse: [ nativeRequest setResponseContentsAsByteArray: contents ]
	