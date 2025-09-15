import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View, TouchableOpacity, Alert } from 'react-native';
import { useState } from 'react';
import * as DocumentPicker from 'expo-document-picker';

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
      // NOTA: Para MVP usamos texto de ejemplo
      // La extracción real de PDF se implementará en v2.0
      const demoText = `Este es un texto de ejemplo para demostrar ReaderChunks mientras desarrollamos en Expo Go. La extracción real de PDF funcionará solo en la versión compilada nativamente. Cada oración se muestra individualmente para crear una experiencia de lectura enfocada. Puedes navegar entre oraciones usando los botones de navegación. Esta funcionalidad te ayuda a concentrarte en una idea a la vez. El objetivo es mejorar la comprensión lectora mediante la lectura pausada. Una vez compilada la app nativa, podrás cargar PDFs reales y extraer su contenido automáticamente.`;

      const sentenceArray = splitIntoSentences(demoText);
      setSentences(sentenceArray);
      setCurrentSentenceIndex(0);
      setShowReader(true);

      const sizeText = fileSize ? `${(fileSize / 1024 / 1024).toFixed(2)} MB` : 'Desconocido';
      const info = `📄 Archivo: ${fileName}
📏 Tamaño: ${sizeText}
📝 Oraciones extraídas: ${sentenceArray.length}
⚠️ Usando texto de ejemplo (MVP v1.0)

NOTA: Esta versión demuestra la funcionalidad
de lectura por oraciones. La extracción real
de PDF se implementará en la v2.0.`;

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