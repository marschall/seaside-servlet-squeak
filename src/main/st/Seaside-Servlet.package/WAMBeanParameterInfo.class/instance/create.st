private
create
	^ (Java type: 'javax.management.MBeanParameterInfo')
		new: name
		type: type
		description: description