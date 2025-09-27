@echo off
echo Extracting Don Quixote text...
java -cp ".;pdfbox-app-2.0.27.jar" PDFTextExtractor "Don Quijote I -- de Cervantes, Saavedra Miguel -- 0 -- f214210ab306fdbabb19fac243f8e01f -- Anna's Archive.pdf" > quijote_extracted.txt
echo Done! Check quijote_extracted.txt