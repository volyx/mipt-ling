package io.github.volyx;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String FILE_NAME_WARS = "/Users/volyx/Projects/mipt-lin/Википедия-20180105212621-комп-игры-clear.xml";
    private static final String FILE_NAME_GAMES = "/Users/volyx/Projects/mipt-lin/Википедия-20180105213039-сражения-clear.xml";
    public static final int COUNT = 10_000;

    public static void main(String args[]) throws IOException {

        for (String file : Arrays.asList(FILE_NAME_WARS, FILE_NAME_GAMES)) {
            long start = System.currentTimeMillis();
            TObjectIntHashMap<String> freq = new TObjectIntHashMap<>();
            Path path = Paths.get(file);
            AtomicInteger counter = new AtomicInteger();
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


        }


    }

}


