package assignment;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

/*
 * CS 314H Assignment 2 - Random Writing
 *
 * Your task is to implement this RandomWriter class
 */


public class RandomWriter implements TextProcessor {

    private int level;
    private String fullText;
    private HashMap<String, List<Character>> analysisTable;

    private final Random RNG = new Random();

    public static void main(String[] args) {
      if (args.length != 4) {
        System.err.println("Usage: RandomWriter <source> <result> <k> <length>");
        return;
      }
      int k, length;
      try {
        k = Integer.parseInt(args[2]);
        length = Integer.parseInt(args[3]);
      } catch (NumberFormatException e) {
        System.err.println("k and length must be integers");
        return;
      }
      if (k < 0 || length < 0) {
        System.err.println("k and length must be non-negative");
        return;
      }
      try {
        TextProcessor p = createProcessor(k);
        p.readText(args[0]);
        p.writeText(args[1], length);
      } catch (IOException e) {
        System.err.println("input and output error: " + e.getMessage());
      }
    }

    // Unless you need extra logic here, you might not have to touch this method
    public static TextProcessor createProcessor(int level) {
      return new RandomWriter(level);
    }

    private RandomWriter(int level) {
      this.level = level;
    }

    public void readText(String inputFilename) throws IOException {

      StringBuilder sb = new StringBuilder();
      try (BufferedReader in = new BufferedReader(new FileReader(inputFilename))) {
        int c;
        while ((c = in.read()) != -1) 
          sb.append((char) c);
      }

      fullText = sb.toString();
      if (fullText.length() <= level) {
        throw new IOException("source must contain more than " + level + " characters");
      }

      // storing seed to next character in hash table for faster lookups
      analysisTable = new HashMap<>();
      for (int i = 0; i < fullText.length() - level; i++) {
        String key = fullText.substring(i, i + level);

        // using computeifabsent so that no operations wasted on prexisting seeds
        analysisTable.computeIfAbsent(key, x -> new ArrayList<>()).add(fullText.charAt(i + level));
      }

    }

    public void writeText(String outputFilename, int length) throws IOException {
      String result = generate(length);
      try (FileWriter out = new FileWriter(outputFilename)) {
          out.write(result);
      }
    }

    String generate(int length) {
      StringBuilder out = new StringBuilder(length);
      String seed = randomSeed();
      for (int i = 0; i < length; i++) {
          List<Character> candidates = getNextPossible(seed);
          if (candidates.isEmpty()) { // no existing seed
              seed = randomSeed();
              candidates = getNextPossible(seed);
          }

          char c = candidates.get(RNG.nextInt(candidates.size()));
          out.append(c);
          seed = level == 0 ? "" : seed.substring(1) + c; // shift the seed one character       
      }

      return out.toString();
    }

    // private helper to abstract getting a random seed from the string
    private String randomSeed() {
      int i = RNG.nextInt(fullText.length() - level);
      return fullText.substring(i, i + level);
    }

    // private helpers to get the list of possible characters given a seed

    private ArrayList<Character> getNextPossibleScan(String seed) { // my original naive approach
      ArrayList<Character> possible = new ArrayList<>();
      for (int i = 0 ; i < fullText.length() - level; i++) {
        if (fullText.startsWith(seed, i)) {
          possible.add(fullText.charAt(i + level));
        }
      }

      return possible;
    }

    List<Character> getNextPossible(String seed) { // more optimized
      // using getOrDefault so an empty list can be easily returned if the seed doesn't exist in the fullText
      return analysisTable.getOrDefault(seed, Collections.emptyList());
    }


}
