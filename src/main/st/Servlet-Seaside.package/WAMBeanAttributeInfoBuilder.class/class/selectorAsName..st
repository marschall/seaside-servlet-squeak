private
selectorAsName: aSelector
	^ (aSelector last = $:
		ifTrue: [ aSelector allButLast ]
		ifFalse: [ aSelector ]) greaseString