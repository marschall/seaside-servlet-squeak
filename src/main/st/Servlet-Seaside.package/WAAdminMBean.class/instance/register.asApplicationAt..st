operations
register: aComponentClassName asApplicationAt: aString
	<operation>
	<impact: #ACTION>
	<parameterName: 'className' type: 'java.lang.String' descritption: 'the name of the component class to register'>
	<parameterName: 'path' type: 'java.lang.String' descritption: 'the path under which to register the component class'>
	WAAdmin
		register: (Smalltalk globals at: aComponentClassName)
		asApplicationAt: aString