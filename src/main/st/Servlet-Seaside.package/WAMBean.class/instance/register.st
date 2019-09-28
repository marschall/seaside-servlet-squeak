public
register
	self platformMBeanServer
		registerMBean: self newDynamicMBean
		objetName: self jmxObjectName