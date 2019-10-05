public
list: aJavaList collect: aOneArgumentBlock
	| collection |
	collection := Array new: aJavaList size.
	self list: aJavaList do: [ :each |
		collection add: (aOneArgumentBlock value: each) ].
	^ collection