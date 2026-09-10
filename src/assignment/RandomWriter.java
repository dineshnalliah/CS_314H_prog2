package assignment;
import java.io.*;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Your task is to implement this RandomWriter class
 */
public class RandomWriter implements TextProcessor {

    public static void main(String[] args) {

    }

    // Unless you need extra logic here, you might not have to touch this method
    public static TextProcessor createProcessor(int level) {
      return new RandomWriter(level);
    }

    private RandomWriter(int level) {
      // Do whatever you want here
    }


    public void readText(String inputFilename) throws IOException {

    }

    public void writeText(String outputFilename, int length) throws IOException {

    }
}
