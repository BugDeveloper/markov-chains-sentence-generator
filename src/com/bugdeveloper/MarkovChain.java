package com.bugdeveloper;

import java.util.HashMap;

/**
 * Created by BugDeveloper on 20.01.2017.
 */
public class MarkovChain {

    private double count;
    private HashMap<String, MarkovChain> chain;

    public MarkovChain() {
        chain = new HashMap<>();
    }

    public void increaseChance() {
        count = getCount() + 1;
    }

    public double getCount() {
        return count;
    }

    public HashMap<String, MarkovChain> getChain() {
        return chain;
    }
}
