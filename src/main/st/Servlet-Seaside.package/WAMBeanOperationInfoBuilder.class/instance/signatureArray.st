private
signatureArray
	^ (signature collect: [ :each |
		each create ]) asArray