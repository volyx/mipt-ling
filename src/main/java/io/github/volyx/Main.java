package io.github.volyx;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String FILE_NAME_WARS = "/Users/volyx/Projects/mipt-lin/Википедия-20180105212621-комп-игры-clear.xml";
    private static final String FILE_NAME_GAMES = "/Users/volyx/Projects/mipt-lin/Википедия-20180105213039-сражения-clear.xml";
    public static final int COUNT = 10_000;

    public static void main(String args[]) throws IOException {
        AtomicInteger warsCounter = new AtomicInteger();
        AtomicInteger gameCounter = new AtomicInteger();
        TObjectIntHashMap<String> freqWars = writeFreqToFile(warsCounter, FILE_NAME_WARS);
        TObjectIntHashMap<String> freqGames = writeFreqToFile(gameCounter, FILE_NAME_GAMES);
        THashSet<String> allWords = new THashSet<>();
        allWords.addAll(freqWars.keySet());
        allWords.addAll(freqGames.keySet());
        Comparator<Row> rowComparator = Comparator.comparingDouble(o -> o.loglike);
        List<Row> rows = new ArrayList<>();
        for (String word : allWords) {
            double a = (double) freqWars.get(word);
            double b = (double) freqGames.get(word);
            double c = warsCounter.get();
            double d = gameCounter.get();
            double E1 = c * (a + b) / (c + d);
            double E2 = d * (a + b) / (c + d);
            double loglike = 2 * (a * ln(a / E1) + b * ln(b / E2));
            Row row = new Row();
            row.word = word;
            row.corpus1 = (int) a;
            row.corpus2 = (int) b;
            row.loglike = loglike;
            rows.add(row);
        }
        rows.sort(rowComparator.reversed());
        for (Row row : rows) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("loglike.tsv"))) {
                try {
                    double loglike = Double.isNaN(row.loglike) ? 0.0 : row.loglike;
                    bw.write(String.format("%s\t%d\t%d\t%.10f\n", row.word, row.corpus1, row.corpus2, loglike));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static double ln(double l) {
        return (-Math.log(1 - l)) / l;
    }

    private static TObjectIntHashMap<String> writeFreqToFile(AtomicInteger counter, String file) throws IOException {
        long start = System.currentTimeMillis();
        TObjectIntHashMap<String> freq = new TObjectIntHashMap<>();
        Path path = Paths.get(file);
        Files.lines(path)
                .filter(s -> s.length() > 200)
                .forEach(s -> {
                    if (s.length() < 200) {
                        throw new RuntimeException();
                    }
                    String[] words = s.replaceAll("[^а-яА-Я ]", "")
                            .toLowerCase().split("\\s+");
                    for (String word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        counter.incrementAndGet();
                        freq.adjustOrPutValue(word, 1, 1);
                    }
                });

        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Duration = " + duration + " sec");
        System.out.println("freq.size() = " + freq.size());
        System.out.println("counter = " + counter);
        int[] sorted = freq.values();
        Arrays.sort(sorted);
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path.getFileName().toString() + ".tsv"))) {

            for (int i = sorted.length - 1; i > sorted.length - COUNT; i--) {
                int topI = sorted[i];
                freq.forEachEntry((a, b) -> {
                    if (topI == b) {

                        try {
                            bw.write(a);
                            bw.write("\t");
                            bw.write(Integer.toString(b));
                            bw.write("\t");
                            bw.write(String.format("%.10f", (double) b / (double) counter.get()));
                            bw.write("\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                });
            }
        }
        return freq;
    }

    public static class Row {
        public String word;
        public int corpus1;
        public int corpus2;
        public double loglike;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Row row = (Row) o;
            return Objects.equals(word, row.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word);
        }
    }
}


