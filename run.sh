javac -cp ".:./jar-files/json-20210307.jar:./jar-files/openpdf-1.1.0.jar" com/*/*/*.java

echo "Compiled Sucessfully"

java -cp ".:./jar-files/json-20210307.jar:./jar-files/openpdf-1.1.0.jar" com.fundexpert.test.KarvyPdfParserTest




