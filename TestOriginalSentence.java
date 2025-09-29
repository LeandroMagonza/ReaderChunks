import java.util.List;

public class TestOriginalSentence {
    public static void main(String[] args) {
        // Simular el texto que llega desde SentenceSegmenter
        String originalFromSegmenter = "habiendo visto por los señores dél un libro intitulado El ingenioso hidalgo de la Mancha, compuesto por Miguel de Cervantes Saavedra, tasaron cada pliego del dicho libro a tres maravedís y medio el cual tiene ochenta y tres pliegos, que al dicho precio monta el dicho libro docientos y noventa maravedís y medio, en que se ha de vender en papel.";

        System.out.println("=== TESTING SENTENCE FROM SEGMENTER ===");
        System.out.println("Original with period: " + originalFromSegmenter.length() + " chars");
        System.out.println("Text: " + originalFromSegmenter);
        System.out.println();

        List<String> parts = DynamicSentenceSplitterTest.splitIfNeeded(originalFromSegmenter);

        System.out.println("Split into " + parts.size() + " parts:");
        for (int i = 0; i < parts.size(); i++) {
            System.out.println((i + 1) + ". [" + parts.get(i).length() + " chars] " + parts.get(i));
        }
        System.out.println();

        // Check specifically the part with "ingenioso hidalgo"
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (part.contains("ingenioso hidalgo")) {
                System.out.println("=== FOUND PROBLEMATIC PART ===");
                System.out.println("Part " + (i + 1) + ": " + part);
                System.out.println("Length: " + part.length());
                System.out.println("Last 20 chars: '" + part.substring(Math.max(0, part.length() - 20)) + "'");
                System.out.println();
            }
        }
    }
}