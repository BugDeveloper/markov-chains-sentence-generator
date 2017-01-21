package com.bugdeveloper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by BugDeveloper on 20.01.2017.
 */
public class SentenceGenerator {

    private HashMap<String, MarkovChain> chain;
    private BufferedReader br;
    private Random random;

    public SentenceGenerator(File input) throws IOException {
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

        while(current.size() != 0) {

            RangeEntry[] wordEntries = getRangeEntries(current);
            String word = chooseWord(wordEntries);

            sentence += word + " ";
            wordNumber++;

            current = current.get(word).getChain();
        }

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

    private String stringMakeover(String string) {

        string = string.toLowerCase();
        string = string.replaceAll("[^A-Za-zА-Яа-я0-9,.<>ё ]", "");
        string = string.replaceAll("<br>"," <br> ");
        string = string.replaceAll(",", " , ");
        string = string.replaceAll("  ", " ");

        string = string.replaceAll("\\.", " . ");

        return string;
    }

    private void initializeChain() throws IOException {
        String line;

        while ((line = br.readLine()) != null) {

            line = stringMakeover(line);

            HashMap<String, MarkovChain> current = chain;

            String[] words = line.split(" ");

            for (int i = 0; i < words.length; i++) {
                if (current.containsKey(words[i]))
                    current.get(words[i]).increaseChance();
                else
                    current.put(words[i], new MarkovChain());

                current = current.get(words[i]).getChain();
            }
        }
    }

}
