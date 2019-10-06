private-jmx
newMBeanInfo
	^ (Java type: 'javax.management.MBeanInfo')
		new: self class name greaseString
		description: self description
		attributes: self attributes
		constructors: self constructors
		operations: self operations
		notifications: self notifications