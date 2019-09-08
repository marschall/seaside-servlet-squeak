converting
partAsByteArray: aPartTruffleObject
	| size inputStream array |
	size := aPartTruffleObject getSize.
	array := ByteArray new: size.
	inputStream := aPartTruffleObject getInputStream.
	"we have no way of doing bulk byte array operations between Java and Squeak"
	[ 1 to: size do: [ :index |
		array at: index put: inputStream read ]
	] ensure: [ inputStream close ].
	^ array