accessing
selector: aSymbol
	aSymbol last = $:
		ifTrue: [ writable := true ]
		ifFalse: [ readable := true ]
	