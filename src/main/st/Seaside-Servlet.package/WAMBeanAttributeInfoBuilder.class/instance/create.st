private
create
	^ (Java type: 'javax.management.MBeanAttributeInfo')
		new: name
		type: type
		description: description
		readable: readable
		writable: writable
		is: is