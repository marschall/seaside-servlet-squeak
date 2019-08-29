private
list: aJavaList do: aOneArgumentBlock
	0 to: aJavaList size - 1 do: [ :i |
		aOneArgumentBlock value: (aJavaList get: i) ]