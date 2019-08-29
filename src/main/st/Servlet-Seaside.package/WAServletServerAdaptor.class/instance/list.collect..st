private
list: aJavaList collect: aOneArgumentBlock
	| collection |
	collection := OrderedCollection new.
	self list: aJavaList do: [ :each |
		collection add: each ].
	^ collection