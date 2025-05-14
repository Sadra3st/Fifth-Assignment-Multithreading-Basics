import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TypingTest {
    private static String playerName = "";
    private static String lastInput = "";
    private static Scanner scanner = new Scanner(System.in);
    private static AtomicBoolean inputReady = new AtomicBoolean(false);
    private static long startTime;
    private static int correctCount = 0;
    private static int incorrectCount = 0;
    private static int noInputCount = 0;
    private static long totalTime = 0;
    private static int score = 0;
    private static boolean exitRequested = false;
    private static final int BASE_SCORE_PER_WORD = 100;
    private static final String SCORES_FILE = "typing_scores.txt";
    private static List<PlayerScore> topScores = new ArrayList<>();

    static class PlayerScore implements Comparable<PlayerScore> {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(PlayerScore other) {
            return Integer.compare(other.score, this.score); // Descending order
        }

        @Override
        public String toString() {
            return name + ": " + score;
        }
    }

    public static class InputRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && !exitRequested) {
                if (!inputReady.get()) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("exit")) {
                        exitRequested = true;
                        break;
                    }
                    lastInput = input;
                    inputReady.set(true);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        loadTopScores();
        showWelcomeScreen();

        try {
            List<String> words = readWordsFromResources();
            List<String> testWords = selectRandomWords(words, 10);

            System.out.println("\nStarting typing test for " + playerName + "...");
            typingTest(testWords);
        } catch (IOException e) {
        }

        saveScore();
        showTopScores();
        scanner.close();
    }

    private static void showWelcomeScreen() {
        System.out.println("====================================");
        System.out.println("        TYPING TEST CHALLENGE       ");
        System.out.println("====================================");

        System.out.print("\nEnter your name: ");
        playerName = scanner.nextLine();

        System.out.println("\nWelcome, " + playerName + "! Here are the rules:");
        System.out.println("1. Words will appear one at a time");
        System.out.println("2. Type the word exactly as shown and press Enter");
        System.out.println("3. You'll earn points for correct answers - faster typing = more points!");
        System.out.println("4. Type 'exit' at any time to quit");
        System.out.println("\nPress Enter to begin...");
        scanner.nextLine();
    }

    private static void typingTest(List<String> inputList) throws InterruptedException {
        Thread inputThread = new Thread(new InputRunnable());
        inputThread.setDaemon(true);
        inputThread.start();

        for (String wordToTest : inputList) {
            if (exitRequested) break;
            testWord(wordToTest);
            if (!exitRequested) {
                Thread.sleep(2000);
            }
        }

        inputThread.interrupt();
        showTestSummary();
    }

    private static void testWord(String wordToTest) {
        try {
            System.out.println("\nType: " + wordToTest);
            System.out.print("> ");
            lastInput = "";
            inputReady.set(false);

            long timeout = wordToTest.length() * 2000L;
            startTime = System.currentTimeMillis();
            long endTime = startTime + timeout;

            while (System.currentTimeMillis() < endTime && !inputReady.get() && !exitRequested) {
                Thread.sleep(100);
            }

            if (exitRequested) return;

            long responseTime = System.currentTimeMillis() - startTime;
            totalTime += responseTime;

            System.out.println();
            if (lastInput.isEmpty()) {
                System.out.println("You didn't type anything!");
                noInputCount++;
            } else {
                if (lastInput.equals(wordToTest)) {
                    int wordScore = calculateScore(responseTime, timeout);
                    score += wordScore;
                    correctCount++;
                    System.out.println("✓ Correct! +" + wordScore + " points (" + responseTime + "ms)");
                } else {
                    System.out.println("✗ Incorrect: " + lastInput);
                    incorrectCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateScore(long responseTime, long timeout) {
        double timeRatio = (double)responseTime / timeout;
        return (int)(BASE_SCORE_PER_WORD * (1.5 - timeRatio)); // 50-150 points
    }

    private static void showTestSummary() {
        System.out.println("\n====================================");
        System.out.println("           TEST SUMMARY            ");
        System.out.println("====================================");
        System.out.println("Player: " + playerName);
        System.out.println("Correct answers: " + correctCount);
        System.out.println("Incorrect answers: " + incorrectCount);
        System.out.println("Missed words: " + noInputCount);
        System.out.println("Total score: " + score);
        System.out.println("Total time: " + (totalTime / 1000.0) + " seconds");
        if (correctCount > 0) {
            System.out.println("Average time per correct answer: " +
                    (totalTime / correctCount) + "ms");
        }
    }

    private static void loadTopScores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    topScores.add(new PlayerScore(
                            parts[0].trim(),
                            Integer.parseInt(parts[1].trim())
                    ));
                }
            }
            Collections.sort(topScores);
        } catch (IOException e) {

        }
    }

    private static void saveScore() {
        topScores.add(new PlayerScore(playerName, score));
        Collections.sort(topScores);

        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE))) {
            for (PlayerScore ps : topScores) {
                writer.println(ps.name + ": " + ps.score);
            }
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }

    private static void showTopScores() {
        System.out.println("\n====================================");
        System.out.println("          TOP PLAYERS              ");
        System.out.println("====================================");

        int count = Math.min(topScores.size(), 5);
        if (count == 0) {
            System.out.println("No scores yet!");
            return;
        }

        for (int i = 0; i < count; i++) {
            System.out.println((i+1) + ". " + topScores.get(i));
        }

      //if you are not in top 5, and I know you will fail Soroush!
        int playerRank = topScores.indexOf(new PlayerScore(playerName, score)) + 1;
        if (playerRank > 5) {
            System.out.println("\nYour rank: " + playerRank + " out of " + topScores.size());
        }
    }

    private static List<String> readWordsFromResources() throws IOException {
        List<String> words = new ArrayList<>();
        try (InputStream is = TypingTest.class.getClassLoader().getResourceAsStream("Words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        }
        return words;
    }

    private static List<String> selectRandomWords(List<String> words, int count) {
        List<String> selected = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < Math.min(count, words.size()); i++) {
            selected.add(words.get(random.nextInt(words.size())));
        }
        return selected;
    }

}