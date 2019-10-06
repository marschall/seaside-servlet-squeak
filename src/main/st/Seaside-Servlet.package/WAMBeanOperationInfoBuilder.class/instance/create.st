private
create
	^ (Java type: 'javax.management.MBeanOperationInfo')
		new: name
		description: description
		signature: self signatureArray
		type: type
		impact: impact