accessing
path: aPathString encoding: anEncodingString
	"Convenience method to start a single adaptor on the specified context path."

	| adaptor |
	self stopAllAdapters.
	adaptor := self default.
	adaptor isNil
		ifFalse: [ adaptor stop ]
		ifTrue: [ adaptor := self new ].
	adaptor requestHandler: (aPathString = ''
		ifTrue: [
			"root context path"
			WADispatcher default ]
		ifFalse: [
			WAContextPathHandler
				onContextPath: aPathString
				requestHandler: WADispatcher default ]).
	adaptor codec: (GRCodec forEncoding: anEncodingString).
	adaptor start.
	^ adaptor
	