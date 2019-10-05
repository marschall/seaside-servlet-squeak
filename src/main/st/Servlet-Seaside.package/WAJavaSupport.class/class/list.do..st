public
list: aJavaList do: aOneArgumentBlock
	0 to: aJavaList size - 1 do: [ :index |
		aOneArgumentBlock value: (aJavaList get: index) ]