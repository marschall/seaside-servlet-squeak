building
createFeaturesFrom: aClass
	| featureBuilders |
	featureBuilders := Dictionary new.
	aClass selectorsAndMethodsDo: [ :selector :method |
		(method pragmas anySatisfy: [ :pragma | pragma keyword = self pragmaKeyword ]) ifTrue: [
			| name featureBuilder |
			name := self selectorAsName: selector.
			featureBuilder := featureBuilders
				at: name
				ifAbsentPut: [ self name: name ].
			featureBuilder selector: selector.
			method pragmas do: [ :pragma |
				(configuration includes: pragma keyword) ifTrue: [
					featureBuilder perform: pragma keyword withArguments: pragma arguments ]  ] ] ].
	^ (featureBuilders values collect: [ :builder |
		builder create ]) asArray