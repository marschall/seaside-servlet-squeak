private-jmx
newDynamicMBean
	^ (Java type: 'com.github.marschall.squeak.servlet.SqueakObjectMBean')
		new: self
		mBeanInfo: self newMBeanInfo