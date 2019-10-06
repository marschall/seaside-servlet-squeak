public
list: aJavaList collect: aOneArgumentBlock
	| collection index |
	collection := Array new: aJavaList size.
	index := 1.
	self list: aJavaList do: [ :each |
		collection
			at: index
			put: (aOneArgumentBlock value: each).
		index := index + 1 ].
	^ collection