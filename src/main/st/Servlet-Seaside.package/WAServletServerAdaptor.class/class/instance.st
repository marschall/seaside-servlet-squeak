accessing
instance
	"Convenience method to start a single adaptor on the specified port. If an existing
	adaptor of this type is already registered, it will be restarted on the specified port."

	| adaptor |
	adaptor := self default.
	adaptor isNil
		ifFalse: [ adaptor stop ]
		ifTrue: [ adaptor := self new ].
	adaptor start.
	^ adaptor
	