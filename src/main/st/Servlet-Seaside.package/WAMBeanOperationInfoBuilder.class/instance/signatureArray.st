private
signatureArray
	^ signature isEmpty
		ifTrue: [ nil ]
		ifFalse: [
			WAJavaSupport
				collect: signature
				with: [ :each | each create ]
				intoArray: 'javax.management.MBeanParameterInfo' ]