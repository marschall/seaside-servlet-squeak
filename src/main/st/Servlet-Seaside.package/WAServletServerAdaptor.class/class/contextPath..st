accessing
contextPath: aString
	"Convenience method to start a single adaptor on the specified context path."

	| adaptor |
	adaptor := self default.
	adaptor isNil
		ifFalse: [ adaptor stop ]
		ifTrue: [ adaptor := self new ].
	adaptor requestHandler: (aString = ''
		ifTrue: [
			"root context path"
			WADispatcher default ]
		ifFalse: [
			WAContextPathHandler
				onContextPath: aString
				requestHandler: WADispatcher default ]).
	adaptor start.
	^ adaptor
	