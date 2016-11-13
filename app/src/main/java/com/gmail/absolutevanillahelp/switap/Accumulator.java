package com.gmail.absolutevanillahelp.switap;

/**
 * Created by derekzhang on 1/20/16.
 */
public class Accumulator {

    private long accumulated;
    private final long total;

    public Accumulator(long total) {

        accumulated = 0;
        this.total = total;
    }

    public void accumulate(long addition) {

        accumulated += addition;
    }

    public boolean removeTotal() {

        if (accumulated >= total) {

            accumulated -= total;
            return true;
        }

        return false;
    }
}
