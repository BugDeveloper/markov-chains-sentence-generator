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
    private int enclosure;
    private int wordNumber;

    public PostGenerator(File input, int enclosure, int wordNumber) throws IOException {
        this.enclosure = enclosure;
        this.wordNumber = wordNumber;

        chain = new HashMap<>();
        br = new BufferedReader(new FileReader(input));
        random = new Random();

        initializeChain();
        System.out.println("Initialization done!");
    }

    public String generateSentence() {

        String sentence = "";
        HashMap<String, MarkovChain> current = chain;

        int wordNumber = 0;

        while(wordNumber < this.wordNumber) {

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

        if (string.charAt(0) == ',' || string.charAt(0) == '.' || string.charAt(0) == ':')
            string = string.substring(2);

        string = string.replaceAll(" , ", ", ");
        string = string.replaceAll(" \\. ", "\\. ");
        string = string.replaceAll(", ,", ",");
        string = string.replaceAll(" : ", ": ");

        for (int i = 0; i < string.length() - 2; i++) {
            if (string.substring(i, i + 2).equals(". "))
                string = string.substring(0, i + 2) + Character.toUpperCase(string.charAt(i + 2)) + string.substring(i + 3);
        }

        string = Character.toUpperCase(string.charAt(0)) + string.substring(1);

        if (string.charAt(string.length() - 1) == ',' || string.charAt(string.length() - 1) == '-' || string.charAt(string.length() - 1) == ':')
            string = string.substring(0, string.length() - 2) + ".";
        else
            string = string.substring(0, string.length() - 1) + ".";

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

                if (enclosure > this.enclosure)
                    current = chain;
            }
        }
    }

}
