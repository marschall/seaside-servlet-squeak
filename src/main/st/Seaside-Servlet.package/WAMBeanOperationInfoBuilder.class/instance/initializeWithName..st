initialization
initializeWithName: aString
	self initialize.
	name := aString.
	self impact: #UNKNOWN.
	self type: 'void'.
	signature := OrderedCollection new