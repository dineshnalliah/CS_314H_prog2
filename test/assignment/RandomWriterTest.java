package assignment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


public class RandomWriterTest {

    @TempDir Path tmp;

    // converts string into temporary file so I can use the path to test methods
    private String sourceFile(String contents) throws IOException {
        Path p = tmp.resolve("src" + contents.hashCode() + ".txt");
        Files.writeString(p, contents);
        return p.toString();
    }

    // gives a processor of type RandomWriter (not TestProcessor) after readText is run
    private RandomWriter processor(int level, String contents) throws IOException {
        RandomWriter rw = (RandomWriter) RandomWriter.createProcessor(level);
        rw.readText(sourceFile(contents));
        return rw;
    }

    @Test
    void testExample() {
        // This is just an example test case
        // You should write more tests

        int a = 1;
        int b = 2;
        int c = 3;

        assertEquals(a + b, c);
    }

    @Test
    void testCreateProcessor() {
        TextProcessor p = RandomWriter.createProcessor(3);
        assertNotNull(p);
        assertInstanceOf(RandomWriter.class, p);
    }

    // tests if readText rejects a source file that has total length equal to k
    @Test
    void testReadTextRejectsKCharacterSource() {
        RandomWriter rw = (RandomWriter) RandomWriter.createProcessor(3);
        assertThrows(IOException.class, () -> rw.readText(sourceFile("abc")));
    }

    // tests if readText throws the IO exception if there's no file at path
    @Test
    void testReadTextThrowsOnMissingFile() {
        RandomWriter rw = (RandomWriter) RandomWriter.createProcessor(2);
        assertThrows(IOException.class, () -> rw.readText(tmp.resolve("does_not_exist.txt").toString()));
    }

    // tests if output file has the correct amount of characters
    @Test
    void testWriteTextCorrectLength() throws IOException {
        RandomWriter rw = processor(2, "the three pirates charted that course");
        Path out = tmp.resolve("out.txt");
        rw.writeText(out.toString(), 250);
        assertEquals(250, Files.readString(out).length());
    }

    // tests if the output file is empty when 0 given as length
    @Test
    void testZeroLengthProducesEmptyFile() throws IOException {
        RandomWriter rw = processor(2, "abcdefghij");
        Path out = tmp.resolve("empty.txt");
        rw.writeText(out.toString(), 0);
        assertTrue(Files.exists(out));
        assertEquals("", Files.readString(out));
    }

    // tests validity of k == 0 and treats all characters equally likely
    @Test
    void testKEqualsZero() throws IOException {
        RandomWriter rw = processor(0, "aab");
        String out = assertDoesNotThrow(() -> rw.generate(500));
        assertEquals(500, out.length());
        for (char c : out.toCharArray()) {
            assertTrue("aab".indexOf(c) >= 0, "unexpected character: " + c);
        }
    }

    // tests that given a uniform string, only that character appears as output
    @Test
    void testUniformSource() throws IOException {
        RandomWriter rw = processor(2, "aaaaaaaa");
        assertEquals("aaaaaaaaaaaaaaaaaaaa", rw.generate(20));
    }

    // test thats that randomwriter gets the correct characters when each seed has only one valid next possible character
    @Test
    void testUniqueSeededSource() throws IOException {
        RandomWriter rw = processor(2, "abcabcabcabc");
        String out = rw.generate(30);
        for (int i = 1; i < out.length(); i++) {
            char prev = out.charAt(i - 1);
            char expected = prev == 'a' ? 'b' : prev == 'b' ? 'c' : 'a';
            assertEquals(expected, out.charAt(i), "cycle broke at " + i);
        }
    }

    // tests that adjacent equal seeds both contribute possible next characters
    @Test
    void testOverlappingSeeds() throws IOException {
        RandomWriter rw = processor(2, "aaab");
        List<Character> followers = rw.getNextPossible("aa");
        assertEquals(2, followers.size());
        assertTrue(followers.contains('a'));
        assertTrue(followers.contains('b'));
    }

    // tests if the possible characters match the true frequency of the source
    // this test can possibly false negative with low probability, on the off chance that the output is 5 standard deviations from the mean
    @Test
    void testCorrectCharacterFrequencies() throws IOException {
        RandomWriter rw = processor(0, "aab");
        String out = rw.generate(6000);
        long aCount = out.chars().filter(c -> c == 'a').count(); // just checking 'a'
        // expected 4000, sd roughly 36.5; +/- 5 sd is a very loose bound
        assertTrue(aCount > 3800 && aCount < 4200, "'a' appeared " + aCount + " times out of 6000, expected near 4000");
    }

    // tests that reaching a deadend seed leads to picking a new seed and doesn't crash
    @Test
    void testDeadEnd() throws IOException {
        RandomWriter rw = processor(2, "abcd");
        // the seed "cd" would have no follow up
        String out = assertDoesNotThrow(() -> rw.generate(50));
        assertEquals(50, out.length());
    }

    // tests that a seed not found returns no characters
    @Test
    void testUnknownSeed() throws IOException {
        RandomWriter rw = processor(2, "abcabc");
        assertTrue(rw.getNextPossible("zz").isEmpty());
    }

}
