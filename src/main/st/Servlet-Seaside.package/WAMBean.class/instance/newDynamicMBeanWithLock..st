private-jmx
newDynamicMBeanWithLock: anObject
	^ (Java type: 'com.github.marschall.squeak.servlet.SqueakObjectMBean')
		new: self
		mBeanInfo: self newMBeanInfo
		lock: anObject