private-jmx
newDynamicMBeanWithLock: anObject
	^ (Java type: 'com.github.marschall.seaside.servlet.squeak.SqueakObjectMBean')
		new: self
		mBeanInfo: self newMBeanInfo
		lock: anObject