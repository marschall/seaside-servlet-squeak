converting-request
toWaFile: aFilePart  "com.github.marschall.seaside.servlet.squeak.FilePart"
	^ WAFile new
			fileName: aFilePart getFileName;
			contentType: (WAMimeType fromString: aFilePart getContentType);
			contents: aFilePart getContents;
			yourself