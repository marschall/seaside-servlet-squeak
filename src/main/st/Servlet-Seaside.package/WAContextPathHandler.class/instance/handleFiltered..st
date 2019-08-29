handling
handleFiltered: aRequestContext
	| consumer |
	consumer := aRequestContext consumer.
	contextPath do: [ :each |
		(consumer atEnd or: [ consumer next ~= each ]) ifTrue: [
			aRequestContext responseGenerator
				notFound;
				respond ] ].
	requestHandler handle: aRequestContext