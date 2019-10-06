public
collect: aSmalltalkCollection with: aBlock intoArray: aClassNameString
	| javaArray size |
	size := aSmalltalkCollection size.
	javaArray := (Java type: 'java.lang.reflect.Array')
		newInstance:  (Java type: aClassNameString)
		size: size.
	1 to: size do: [ :index |
		javaArray
			at: index - 1
			put: (aBlock value: (aSmalltalkCollection at: index)) ].
	^ javaArray