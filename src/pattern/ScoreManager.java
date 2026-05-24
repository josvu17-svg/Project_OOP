package pattern;

import java.io.*;
import java.util.*;

/**
 * Manages high scores - saves/loads from a file next to the JAR.
 */
public class ScoreManager {
    private static final int MAX_SCORES = 6;
    private static final String FILENAME = "scores.dat";
    private static final List<int[]> scores = new ArrayList<>(); // [score, level, lines]
    private static final List<String> names  = new ArrayList<>();

    static { load(); }

    public static void addScore(String name, int score, int level, int lines) {
        names.add(name);
        scores.add(new int[]{score, level, lines});
        // Sort descending by score
        List<int[]> combined = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            combined.add(new int[]{scores.get(i)[0], scores.get(i)[1], scores.get(i)[2], i});
        }
        combined.sort((a, b) -> b[0] - a[0]);
        List<String> sortedNames  = new ArrayList<>();
        List<int[]>  sortedScores = new ArrayList<>();
        for (int[] row : combined) {
            sortedNames.add(names.get(row[3]));
            sortedScores.add(scores.get(row[3]));
        }
        names.clear();  names.addAll(sortedNames);
        scores.clear(); scores.addAll(sortedScores);
        // Keep top MAX_SCORES
        while (names.size() > MAX_SCORES) { names.remove(names.size()-1); scores.remove(scores.size()-1); }
        save();
    }

    public static List<String> getNames()  { return Collections.unmodifiableList(names); }
    public static List<int[]>  getScores() { return Collections.unmodifiableList(scores); }
    public static int          getCount()  { return names.size(); }

    private static File getFile() {
        try {
            File jar = new File(ScoreManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile();
            return new File(jar, FILENAME);
        } catch (Exception e) { return new File(FILENAME); }
    }

    private static void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(getFile()))) {
            for (int i = 0; i < names.size(); i++) {
                int[] s = scores.get(i);
                pw.println(names.get(i) + "," + s[0] + "," + s[1] + "," + s[2]);
            }
        } catch (Exception ignored) {}
    }

    private static void load() {
        try (BufferedReader br = new BufferedReader(new FileReader(getFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    names.add(parts[0]);
                    scores.add(new int[]{Integer.parseInt(parts[1]),
                                         Integer.parseInt(parts[2]),
                                         Integer.parseInt(parts[3])});
                }
            }
        } catch (Exception ignored) {}
    }
}
