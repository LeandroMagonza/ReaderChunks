import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, TouchableOpacity, Alert, NativeModules } from 'react-native';
import { useState } from 'react';
import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system';

const { PDFTextExtractorModule } = NativeModules;

export default function App() {
  const [pdfInfo, setPdfInfo] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [sentences, setSentences] = useState<string[]>([]);
  const [currentSentenceIndex, setCurrentSentenceIndex] = useState(0);
  const [showReader, setShowReader] = useState(false);

  const pickPDF = async () => {
    try {
      setIsLoading(true);
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/pdf',
        copyToCacheDirectory: true,
      });

      if (result.canceled) {
        setIsLoading(false);
        return;
      }

      const file = result.assets[0];
      await processPDF(file.uri, file.name, file.size);
    } catch (error) {
      console.error('Error picking PDF:', error);
      Alert.alert('Error', 'Failed to pick PDF file');
      setIsLoading(false);
    }
  };

  const splitIntoSentences = (text: string): string[] => {
    return text
      .split(/[.!?]+/)
      .map(sentence => sentence.trim())
      .filter(sentence => sentence.length > 0)
      .map(sentence => sentence + '.');
  };

  const processPDF = async (uri: string, fileName: string, fileSize?: number) => {
    try {
      // Input validation
      if (!uri || typeof uri !== 'string' || uri.trim().length === 0) {
        throw new Error('Invalid file URI provided');
      }

      if (!fileName || typeof fileName !== 'string') {
        throw new Error('Invalid file name provided');
      }

      // Check if file appears to be a PDF
      if (!fileName.toLowerCase().endsWith('.pdf')) {
        Alert.alert('Invalid File', 'Please select a PDF file');
        setIsLoading(false);
        return;
      }

      let extractedText = '';
      let isNativeExtraction = false;

      // Try to use native PDF extraction first
      if (PDFTextExtractorModule && PDFTextExtractorModule.extractText) {
        try {
          console.log('Starting PDF extraction process...');
          console.log('Original file URI:', uri);

          // First, try to pass the URI directly to the native module
          try {
            console.log('Attempting direct URI extraction...');
            extractedText = await PDFTextExtractorModule.extractText(uri);
            isNativeExtraction = true;
            console.log('Successfully extracted text using direct URI');
          } catch (directError: any) {
            console.log('Direct URI failed, trying file copy approach:', directError?.message || directError);

            // If direct URI fails, copy the file to a temp location
            const timestamp = Date.now().toString();
            const tempFileName = `temp_pdf_${timestamp}.pdf`;

            try {
              // Create a more robust file copy approach
              console.log('Creating temp file for PDF extraction...');

              // Use a simpler approach - copy to app's cache directory
              const tempDir = FileSystem.cacheDirectory;
              const tempFilePath = `${tempDir}${tempFileName}`;

              console.log('Temp file path:', tempFilePath);

              // Copy the file using expo-file-system
              await FileSystem.copyAsync({
                from: uri,
                to: tempFilePath
              });
              console.log('File copied successfully to:', tempFilePath);

              // Extract using the copied file path (remove file:// prefix for native module)
              const nativeFilePath = tempFilePath.replace('file://', '');
              console.log('Extracting from copied file:', nativeFilePath);

              extractedText = await PDFTextExtractorModule.extractText(nativeFilePath);
              isNativeExtraction = true;
              console.log('Successfully extracted text from copied file');

              // Clean up the temporary file
              try {
                await FileSystem.deleteAsync(tempFilePath);
                console.log('Temp file cleaned up');
              } catch (cleanupError) {
                console.warn('Failed to cleanup temp file:', cleanupError);
              }

            } catch (copyError: any) {
              console.error('File copy failed:', copyError);
              throw new Error(`Failed to copy file for extraction: ${copyError?.message || copyError}`);
            }
          }

        } catch (nativeError: any) {
          console.error('Native PDF extraction failed:', nativeError);

          // Provide more user-friendly error messages
          let errorMessage = 'Unknown error occurred';
          const errorMsg = nativeError?.message || String(nativeError);

          if (errorMsg) {
            if (errorMsg.includes('FILE_NOT_FOUND')) {
              errorMessage = 'PDF file could not be found or accessed';
            } else if (errorMsg.includes('ENCRYPTED_PDF')) {
              errorMessage = 'This PDF is password protected and cannot be processed';
            } else if (errorMsg.includes('undefined')) {
              errorMessage = 'File system access error. Please try selecting a different PDF file.';
            } else {
              errorMessage = errorMsg;
            }
          }

          Alert.alert('PDF Processing Error', errorMessage);
          setIsLoading(false);
          return;
        }
      } else {
        // No native module available
        throw new Error('PDF extraction module not available');
      }

      const sentenceArray = splitIntoSentences(extractedText);
      setSentences(sentenceArray);
      setCurrentSentenceIndex(0);
      setShowReader(true);

      const sizeText = fileSize ? `${(fileSize / 1024 / 1024).toFixed(2)} MB` : 'Desconocido';
      const info = `📄 Archivo: ${fileName}
📏 Tamaño: ${sizeText}
📝 Oraciones extraídas: ${sentenceArray.length}
${isNativeExtraction ? '✅ Extracción nativa de PDF exitosa!' : '⚠️ Usando texto de ejemplo'}
📊 Caracteres extraídos: ${extractedText.length}

${isNativeExtraction
  ? 'La extracción real de PDF está funcionando correctamente.'
  : 'NOTA: La extracción real funciona solo en la versión compilada nativamente. En Expo Go se usa texto de demostración.'
}`;

      setPdfInfo(info);
      setIsLoading(false);
    } catch (error) {
      console.error('Error processing PDF:', error);
      Alert.alert('Error', 'Failed to process PDF file');
      setIsLoading(false);
    }
  };

  const nextSentence = () => {
    if (currentSentenceIndex < sentences.length - 1) {
      setCurrentSentenceIndex(currentSentenceIndex + 1);
    }
  };

  const previousSentence = () => {
    if (currentSentenceIndex > 0) {
      setCurrentSentenceIndex(currentSentenceIndex - 1);
    }
  };

  if (showReader && sentences.length > 0) {
    return (
      <View style={styles.readerContainer}>
        <View style={styles.header}>
          <TouchableOpacity
            style={styles.backButton}
            onPress={() => setShowReader(false)}
          >
            <Text style={styles.backButtonText}>← Volver</Text>
          </TouchableOpacity>
          <Text style={styles.progress}>
            {currentSentenceIndex + 1} / {sentences.length}
          </Text>
        </View>

        <View style={styles.sentenceContainer}>
          <Text style={styles.sentence}>
            {sentences[currentSentenceIndex]}
          </Text>
        </View>

        <View style={styles.navigation}>
          <TouchableOpacity
            style={[styles.navButton, currentSentenceIndex === 0 && styles.navButtonDisabled]}
            onPress={previousSentence}
            disabled={currentSentenceIndex === 0}
          >
            <Text style={[styles.navButtonText, currentSentenceIndex === 0 && styles.navButtonTextDisabled]}>
              ← Anterior
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.navButton, currentSentenceIndex === sentences.length - 1 && styles.navButtonDisabled]}
            onPress={nextSentence}
            disabled={currentSentenceIndex === sentences.length - 1}
          >
            <Text style={[styles.navButtonText, currentSentenceIndex === sentences.length - 1 && styles.navButtonTextDisabled]}>
              Siguiente →
            </Text>
          </TouchableOpacity>
        </View>

        <StatusBar style="auto" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>📚 ReaderChunks</Text>
      <Text style={styles.subtitle}>Selector de PDF</Text>

      <TouchableOpacity
        style={styles.button}
        onPress={pickPDF}
        disabled={isLoading}
      >
        <Text style={styles.buttonText}>
          {isLoading ? '⏳ Procesando...' : '📄 Elegir PDF'}
        </Text>
      </TouchableOpacity>

      {pdfInfo && (
        <View style={styles.infoContainer}>
          <Text style={styles.infoText}>{pdfInfo}</Text>
        </View>
      )}

      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  readerContainer: {
    flex: 1,
    backgroundColor: '#fff',
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 40,
    paddingBottom: 20,
  },
  backButton: {
    padding: 10,
  },
  backButtonText: {
    fontSize: 16,
    color: '#007AFF',
    fontWeight: '600',
  },
  progress: {
    fontSize: 16,
    color: '#666',
    fontWeight: '500',
  },
  sentenceContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  sentence: {
    fontSize: 24,
    lineHeight: 36,
    textAlign: 'center',
    color: '#333',
    fontWeight: '400',
  },
  navigation: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingBottom: 40,
  },
  navButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
    minWidth: 100,
  },
  navButtonDisabled: {
    backgroundColor: '#ccc',
  },
  navButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
    textAlign: 'center',
  },
  navButtonTextDisabled: {
    color: '#999',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 30,
  },
  button: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 30,
    paddingVertical: 15,
    borderRadius: 10,
    marginBottom: 20,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: '600',
  },
  infoContainer: {
    backgroundColor: '#f5f5f5',
    padding: 20,
    borderRadius: 12,
    marginTop: 20,
    width: '100%',
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  infoText: {
    fontSize: 14,
    lineHeight: 22,
    color: '#333',
  },
});