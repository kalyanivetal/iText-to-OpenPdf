## About Task
	
```
	Problem Statement: The given code uses iText library is for creating and manipulating PDF files in Java.
			   The same can be done by using OpenPdf.
	Task: Replace the iText library with OpenPdf with maintaining the functionality of current code.
```

### Depedencies	
* [java]()-11.0.10 Langauge used
* [iText]()- [iText Pdf Parsing Library](https://itextpdf.com/en) iText is a library for creating and manipulating PDF files in Java.
* [OpenPdf]() - [OpenPdf Parsing Library]( https://github.com/LibrePDF/OpenPDF) OpenPDF is a free Java library for creating and editing PDF files with a LGPL and MPL open source license. OpenPDF is based on a fork of iText. 


### Download link - Clone this repository in your local machine 

```
	git clone https://github.com/kalyanivetal/iText-to-OpenPdf
```

### Steps:

```
	cd iText-to-OpenPdf
	
	javac -cp ".:./jar-files/json-20210307.jar:./jar-files/openpdf-1.1.0.jar" com/*/*/*.java

	java -cp ".:./jar-files/json-20210307.jar:./jar-files/openpdf-1.1.0.jar" com.fundexpert.test.KarvyPdfParserTest

```

	## OR 

```
	chmod +x run.sh
	./run.sh
```


