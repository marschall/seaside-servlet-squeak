private
list: aJavaList collect: aOneArgumentBlock
	| collection |
	collection := OrderedCollection new: aJavaList size.
	self list: aJavaList do: [ :each |
		collection add: (aOneArgumentBlock value: each) ].
	^ collection