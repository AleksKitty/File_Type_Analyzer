package analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String pathToFiles = args[0];
        String pathPatterns = args[1];

        ArrayList<Pattern> patterns = getPatternsAndResults(pathPatterns);

        searchPatterns(pathToFiles, patterns);
    }

    private static void searchPatterns(String pathToFiles, ArrayList<Pattern> patterns) throws IOException, InterruptedException, ExecutionException {

        File[] files = new File(pathToFiles).listFiles();

        List<String> fileNames = new ArrayList<>();
        List<Callable<String>> callables = new ArrayList<>();


        if (files != null) {
            for (File file : files) {
                boolean isMatched = false;
                String text = new String(Files.readAllBytes(file.toPath()));
                fileNames.add(file.getName());

                for (int i = patterns.size() - 1; i >= 0; i--) {

                    String pattern = patterns.get(i).getPattern();
                    String result = patterns.get(i).getResult();
                    if (rabinKarpAlgorithm(text, pattern)) {
                        callables.add(() -> result);
                        isMatched = true;
                        break;
                    }
                }

                if (!isMatched) {
                    callables.add(() -> "Unknown file type");
                }
            }

            ExecutorService executor = Executors.newFixedThreadPool(10);

            List<Future<String>> futures = executor.invokeAll(callables);
            for (int i = 0; i < fileNames.size(); i++) {
                System.out.println(fileNames.get(i) + ": " + futures.get(i).get());
            }
        }
    }

    private static ArrayList<Pattern> getPatternsAndResults(String patternsPath) throws FileNotFoundException {

        File file = new File(patternsPath);
        Scanner scanner = new Scanner(file);

        ArrayList<Pattern> patterns = new ArrayList<>();
        String line;
        int indexFirstQuotes;
        int indexSecondQuotes;
        int indexThirdQuotes;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            indexFirstQuotes = line.indexOf("\"");
            indexSecondQuotes = line.indexOf("\"", indexFirstQuotes + 1);
            indexThirdQuotes = line.indexOf("\"", indexSecondQuotes + 1);
            patterns.add(new Pattern(line.charAt(0), line.substring(indexFirstQuotes + 1, indexSecondQuotes), line.substring(indexThirdQuotes + 1, line.length() - 1)));
        }
        scanner.close();

        return patterns;
    }

    private static boolean rabinKarpAlgorithm(String text, String pattern) {
        boolean result = false;

        if (pattern.length() > text.length()) {
            return false;
        }

        int patternHash = hash(pattern);

        int l = pattern.length();
        int i = text.length() - l;
        String subStr;
        int hash = 0;
        // only for avoid not initialized ex
        char removedLetter = 'a';
        while(i >= 0) {

            subStr = text.substring(i, i + l);

            if (i == text.length() - l) {
                hash = hash(subStr);
            } else {
                hash = subHash(hash, subStr.charAt(0), removedLetter, l - 1);
                hash = hash(subStr);
            }

            if (patternHash == hash && pattern.equals(subStr)) {
                result = true;
                break;
            }

            removedLetter =  subStr.charAt(subStr.length() - 1);
            i -= 1;
        }

        return result;
    }

    private static int hash(String s) {
        // constants for hashing
        int mod = 101;
        int a = 256;

        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            sum += s.charAt(i) * Math.pow(a, i);
        }

        return Math.floorMod(sum, mod);
    }
    private static int subHash(int prevHash, char newLetter, char removedLetter, int removedIndex) {
        // constants for hashing
        int mod = 101;
        int a = 256;

        // sth is wrong here
        int hash = (int)(prevHash - removedLetter * Math.pow(a, removedIndex)) * a + newLetter;

        return Math.floorMod(hash, mod);
    }

    private static boolean kmpMethod(String text, String pattern) {
        int[] prefix = calculatePrefix(text);
        return kmpAlgorithm(text, pattern, prefix);
    }

    private static int[] calculatePrefix(String s) {

        int length = s.length();
        int[] prefix = new int[length];

        for (int i = 1; i < length; i++) {

            int j = prefix[i - 1];

            while (j > 0 && s.charAt(i) != s.charAt(j)) {
                j = prefix[j - 1];
            }

            if (s.charAt(i) == s.charAt(j)) {
                j++;
            }

            prefix[i] = j;
        }

        return prefix;
    }

    private static boolean kmpAlgorithm(String text, String pattern, int[] prefix) {

        boolean isFound = false;

        int textLength = text.length();
        int patternLength = pattern.length();

        int j = 0; // index for pattern
        int i = 0; // index for text
        while (i < textLength) {
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }

            if (j == patternLength) {
//               Found pattern at index: i - j
                j = prefix[j - 1];
                isFound = true;
                break;
            }

            // mismatch after j matches
            else if (i < textLength && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0)
                    j = prefix[j - 1];
                else
                    i = i + 1;
            }
        }

        return isFound;
    }
}
