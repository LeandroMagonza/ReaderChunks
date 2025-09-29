import java.util.List;

public class TestNewAlgorithms {

    public static void main(String[] args) {
        System.out.println("=== Testing New Algorithms ===\n");

        // Test 1: SentenceSegmenter - paragraph splitting
        testSentenceSegmenter();

        // Test 2: DynamicSentenceSplitter - character position tracking
        testDynamicSentenceSplitter();

        // Test 3: The problematic text that caused the bug
        testProblematicText();
    }

    private static void testSentenceSegmenter() {
        System.out.println("1. Testing SentenceSegmenter:");
        System.out.println("================================");

        String testText = "Primer párrafo con texto normal.\n\nSegundo párrafo que contiene el texto !a Mancha muy importante.\n\nTercer párrafo final.";

        List<String> paragraphs = SentenceSegmenter.segmentIntoSentences(testText);

        for (int i = 0; i < paragraphs.size(); i++) {
            System.out.println("Paragraph " + i + ": \"" + paragraphs.get(i) + "\"");
        }
        System.out.println("Total paragraphs: " + paragraphs.size());
        System.out.println();
    }

    private static void testDynamicSentenceSplitter() {
        System.out.println("2. Testing DynamicSentenceSplitter:");
        System.out.println("===================================");

        String paragraph = "Este es un párrafo muy largo que debería ser dividido en sub-oraciones más pequeñas; contiene varios signos de puntuación: comas, punto y coma, y otros elementos que ayudan a determinar los mejores puntos de corte para una lectura más cómoda.";

        DynamicSentenceSplitter splitter = new DynamicSentenceSplitter(paragraph);

        System.out.println("Original paragraph (" + paragraph.length() + " chars):");
        System.out.println("\"" + paragraph + "\"\n");

        System.out.println("Sub-sentences:");
        String subSentence;
        int count = 0;
        while ((subSentence = splitter.getNext()) != null) {
            count++;
            int charPos = splitter.getCurrentCharPosition();
            System.out.println(count + ". [pos " + charPos + "] \"" + subSentence + "\"");
        }
        System.out.println();
    }

    private static void testProblematicText() {
        System.out.println("3. Testing Problematic Text (the bug case):");
        System.out.println("===========================================");

        String problematicText = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lentejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda. El resto della concluían sayo de velarte, calzas de velludo para las fiestas con sus pantuflos de lo mismo, los días de entre semana se honraba con su vellori de lo más fino. Tenía en su casa una ama que pasaba de los cuarenta, y una sobrina que no llegaba a los veinte, y un mozo de campo y plaza, que así ensillaba el rocín como tomaba la podadera. Frisaba la edad de nuestro hidalgo con los cincuenta años; era de complexión recia, seco de carnes, enjuto de rostro, gran madrugador y amigo de la caza. Quieren decir que tenía el sobrenombre de Quijada o Quesada (que en esto hay alguna diferencia en los autores que deste caso escriben), aunque por conjeturas verosímiles se deja entender que se llamaba Quijana. Pero esto importa poco a nuestro cuento; basta que en la narración dél no se salga un punto de la verdad.";

        // Test paragraph segmentation
        String textWithBreaks = "Primer párrafo antes.\n\n" + problematicText + "\n\nPárrafo después.";
        List<String> paragraphs = SentenceSegmenter.segmentIntoSentences(textWithBreaks);

        System.out.println("Found " + paragraphs.size() + " paragraphs");
        System.out.println("Middle paragraph contains '!a': " + paragraphs.get(1).contains("!a"));
        System.out.println("Middle paragraph contains 'Mancha': " + paragraphs.get(1).contains("Mancha"));

        // Test that the text is preserved exactly
        String middleParagraph = paragraphs.get(1);
        System.out.println("Text around 'Mancha':");
        int manchaIndex = middleParagraph.indexOf("Mancha");
        if (manchaIndex > 10) {
            String context = middleParagraph.substring(manchaIndex - 10, Math.min(manchaIndex + 20, middleParagraph.length()));
            System.out.println("\"" + context + "\"");
        }

        // Test dynamic splitting
        DynamicSentenceSplitter splitter = new DynamicSentenceSplitter(middleParagraph);
        System.out.println("\nFirst few sub-sentences:");
        for (int i = 0; i < 3; i++) {
            String sub = splitter.getNext();
            if (sub != null) {
                System.out.println((i + 1) + ". \"" + sub + "\"");
            }
        }

        System.out.println();
    }
}