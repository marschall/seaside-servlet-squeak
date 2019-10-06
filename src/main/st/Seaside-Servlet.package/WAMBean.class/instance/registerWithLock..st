public
registerWithLock: anObject
	| dynamicMBean jmxObjectName platformMBeanServer |
	jmxObjectName := self jmxObjectName.
	platformMBeanServer := self platformMBeanServer.
	(platformMBeanServer isRegistered: jmxObjectName) ifTrue: [
		^ nil ].
	dynamicMBean := self newDynamicMBeanWithLock: anObject.
	platformMBeanServer
		registerMBean: dynamicMBean
		objetName: jmxObjectName.
	^ dynamicMBean