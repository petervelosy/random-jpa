package com.github.kuros.random.jpa.random;

/**
 * Created by Kumar Rohit on 5/14/15.
 */
public interface Randomize {

    <T> T createRandom(Class<T> type);
}