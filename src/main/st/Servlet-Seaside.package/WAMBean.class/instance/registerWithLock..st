public
registerWithLock: anObject
	self platformMBeanServer
		registerMBean: (self newDynamicMBeanWithLock: anObject)
		objetName: self jmxObjectName