attributes
developmentToolsEnabled: aBoolean 
	<attribute>
	<type: 'boolean'>
	aBoolean
		ifTrue: [WAAdmin enableDevelopmentTools]
		ifFalse: [WAAdmin disableDevelopmentTools]