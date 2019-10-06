public
stopAllAdapters
	WAServerManager default adaptors do: [ :each | each stop ]