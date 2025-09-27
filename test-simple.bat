@echo off
echo Testing with example.pdf first...
java -cp ".;pdfbox-app-2.0.27.jar" PDFTextExtractor example.pdf > example_output.txt 2>&1
type example_output.txt