package com.bugdeveloper;

/**
 * Created by Nya on 21.01.2017.
 */
public class RangeEntry {

    public String getWord() {
        return word;
    }

    private String word;
    private double lowerBound, upperBound;


    public RangeEntry(String word, double lowerBound, double upperBound) {
        this.word = word;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean isInRange(double number) {

        if (number > lowerBound && number < upperBound)
            return true;

        System.out.println(number + " is not in range of " + lowerBound + "-" + upperBound);
        return false;
    }

    public boolean isUpper(double number) {

        if (number > upperBound)
            return true;

        return false;
    }
}
