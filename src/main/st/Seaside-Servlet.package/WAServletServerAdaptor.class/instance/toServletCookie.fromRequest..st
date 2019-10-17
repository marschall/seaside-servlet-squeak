converting
toServletCookie: aSeasideCookie fromRequest: aNativeRequest
	| servletCookie |
	servletCookie := aNativeRequest newCookie: aSeasideCookie key _: aSeasideCookie value.
	"when deleting a cookie we will have a WARequestCookie which has no version"
	aSeasideCookie version ifNotNilDo: [ :version | servletCookie setVersion: version ].
	aSeasideCookie comment ifNotNilDo: [ :comment | servletCookie setComment: comment ].
	aSeasideCookie domain ifNotNilDo: [ :domain | servletCookie setDomain: domain ].
	aSeasideCookie maxAge ifNotNilDo: [ :maxAge | servletCookie setMaxAge: maxAge ].
	servletCookie setPath: aSeasideCookie path.
	servletCookie setSecure: aSeasideCookie secure.
	servletCookie setHttpOnly: aSeasideCookie httpOnly.
	^ servletCookie
	