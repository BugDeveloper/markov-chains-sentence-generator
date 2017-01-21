package com.bugdeveloper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by BugDeveloper on 20.01.2017.
 */
public class PostGenerator {

    private HashMap<String, MarkovChain> chain;
    private BufferedReader br;
    private Random random;
    private int lowerEnclosure, upperEnclosure, lowerWordNumber, upperWordNumber, currentWordNumber, currentEnclosure;
    private String quotes, pictures;

    public PostGenerator(String quotes, String pictures, int lowerEnclosure, int upperEnclosure, int lowerWordNumber, int upperWordNumber) throws IOException {
        this.lowerEnclosure = lowerEnclosure;
        this.upperEnclosure = upperEnclosure;
        this.lowerWordNumber = lowerWordNumber;
        this.upperWordNumber = upperWordNumber;
        this.quotes = quotes;
        this.pictures = pictures;

        random = new Random();

        currentWordNumber = random.nextInt(upperWordNumber - lowerWordNumber) + lowerWordNumber;
        currentEnclosure = random.nextInt(upperEnclosure - lowerEnclosure) + lowerEnclosure;

        if (currentEnclosure > currentWordNumber)
            currentEnclosure = random.nextInt(currentWordNumber + 1 - lowerEnclosure) + lowerEnclosure + 1;

        chain = new HashMap<>();
        br = new BufferedReader(new FileReader(new File(quotes)));

        initializeChain();
        System.out.println("Initialization done!");
    }

    private String generateSentence() {

        String sentence = "";
        HashMap<String, MarkovChain> current = chain;

        int wordNumber = 0;

        while(wordNumber < this.currentWordNumber) {

            RangeEntry[] wordEntries = getRangeEntries(current);
            String word = chooseWord(wordEntries);

            sentence += word + " ";
            wordNumber++;

            current = current.get(word).getChain();

            if (current.size() == 0)
                current = chain;
        }

        sentence = stringOutMakeover(sentence);

        return sentence;

    }

    private String generatePicture() throws IOException {
        int number = random.nextInt(countLines(pictures));
        int count = 0;
        String line;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(pictures));

        while((line = bufferedReader.readLine()) != null) {
            count++;
            if (count == number)
                return line;
        }
        return "";
    }

    private int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public String[] generatePost() throws IOException {

        String[] post = new String[2];

        post[0] = generateSentence();
        post[1] = generatePicture();

        return post;
    }

    private String chooseWord(RangeEntry[] entries) {

        double rand = random.nextDouble();
        int lowerBound = 0, upperBound = entries.length, midBound, index = -1;

        while (lowerBound < upperBound) {
            midBound = (lowerBound + upperBound) / 2;

            if (entries[midBound].isInRange(rand)) {
                index = midBound;
                break;
            }
            else {
                if (entries[midBound].isUpper(rand))
                    lowerBound = midBound + 1;
                else
                    upperBound = midBound;
            }
        }

        return entries[index].getWord();

    }

    private RangeEntry[] getRangeEntries(HashMap<String, MarkovChain> chain) {

        int i = 0;
        double lastBound = 0;
        long sum = getCountSum(chain);

        RangeEntry[] words = new RangeEntry[chain.size()];

        for (Map.Entry<String, MarkovChain> entry : chain.entrySet()) {

            double upperBound = entry.getValue().getCount() / sum + lastBound;

            words[i] = new RangeEntry(entry.getKey(), lastBound, upperBound);

            lastBound = upperBound;

            i++;
        }

        return words;
    }

    private long getCountSum(HashMap<String, MarkovChain> chain) {

        long sum = 0;

        for (Map.Entry<String, MarkovChain> entry : chain.entrySet()) {
            sum += entry.getValue().getCount();
        }

        return sum;
    }

    private String stringOutMakeover(String string) {

        while (string.charAt(0) == ',' || string.charAt(0) == '.' || string.charAt(0) == ':')
            string = string.substring(2);

        string = string.replaceAll(",,", ",");
        string = string.replaceAll(" , ", ", ");
        string = string.replaceAll(" \\. ", "\\. ");
        string = string.replaceAll(" \\.", "\\. ");
        string = string.replaceAll(", ,", ",");
        string = string.replaceAll(" : ", ": ");
        string = string.replaceAll("\\.,", ",");
        string = string.replaceAll(" - ", "-");

        for (int i = 0; i < string.length() - 2; i++) {
            if (string.substring(i, i + 2).equals(". "))
                string = string.substring(0, i + 2) + Character.toUpperCase(string.charAt(i + 2)) + string.substring(i + 3);
        }

        string = Character.toUpperCase(string.charAt(0)) + string.substring(1);

        if (string.charAt(string.length() - 1) == ',' || string.charAt(string.length() - 1) == '-' || string.charAt(string.length() - 1) == ':')
            string = string.substring(0, string.length() - 1) + ".";
        else
        if (string.charAt(string.length() - 2) == ',' || string.charAt(string.length() - 2) == '-' || string.charAt(string.length() - 2) == ':')
            string = string.substring(0, string.length() - 2) + ".";
        else
            string = string + ".";

        string = string.replaceAll(" \\.", ".");

        return string;
    }

    private String stringInMakeover(String string) {

        //crouch

        string = string.toLowerCase();
        string = string.replaceAll("\\p{Pd}", "-");
        string = string.replaceAll("[^A-Za-zА-Яа-я0-9,ё.:\\- ]", "");
        string = string.replaceAll("br", "");

        string = string.replaceAll("-"," - ");
        string = string.replaceAll(":"," : ");
        string = string.replaceAll(",", " , ");
        string = string.replaceAll("  ", " ");
        string = string.replace("\\.", " . ");

        return string;
    }

    private void initializeChain() throws IOException {

        String line;

        while ((line = br.readLine()) != null) {

            line = stringInMakeover(line);

            HashMap<String, MarkovChain> current = chain;

            String[] words = line.split(" ");

            int enclosure = 0;

            for (int i = 0; i < words.length; i++) {

                if (!current.containsKey(words[i]))
                    current.put(words[i], new MarkovChain());

                current.get(words[i]).increaseChance();

                current = current.get(words[i]).getChain();
                enclosure++;

                if (enclosure > currentEnclosure)
                    current = chain;
            }
        }
    }

}