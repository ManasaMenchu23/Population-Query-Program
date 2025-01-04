import javafx.util.Pair;
import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PopulationQuery {

    private static final int TOKENS_PER_LINE = 7;
    private static final int POPULATION_INDEX = 4;
    private static final int LATITUDE_INDEX = 5;
    private static final int LONGITUDE_INDEX = 6;
    private static final String FILE_NAME = "CenPop2010.txt"; // Adjust to your file path
    private static final int GRID_SIZE = 1000; // Example grid size, adjust as needed
    private static final Lock lock = new ReentrantLock(); // Lock for thread safety

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java PopulationQuery west north east south");
            System.exit(1);
        }

        float west = Float.parseFloat(args[0]);
        float north = Float.parseFloat(args[1]);
        float east = Float.parseFloat(args[2]);
        float south = Float.parseFloat(args[3]);

        int columns = getColumns();
        int rows = getRows();
        int version = getVersionNum();

        preprocess(FILE_NAME, columns, rows, version);

        Pair<Integer, Float> result = PopulationQuery.singleInteraction(west, south, east, north);
        int population = result.getKey();
        System.out.println("Population: " + population);
    }

    public static Pair<Integer, Float> singleInteraction(float west, float south, float east, float north) {
        long population = queryPopulation(west, north, east, south);
        return new Pair<>(Math.toIntExact(population), 0.0f); // Replace 0.0f with any other relevant float value if needed
    }

    public static void preprocess(String filename, int columns, int rows, int version) {
        ForkJoinPool pool = new ForkJoinPool();
        List<PreprocessTask> tasks = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            tasks.add(new PreprocessTask(filename, i, GRID_SIZE, columns, rows, version));
        }

        for (PreprocessTask task : tasks) {
            pool.submit(task);
        }

        for (PreprocessTask task : tasks) {
            task.join(); // Ensures each task completes before continuing
        }
    }

    // Method to query population using ForkJoinPool
    public static long queryPopulation(float west, float north, float east, float south) {
        ForkJoinPool pool = new ForkJoinPool();
        QueryTask queryTask = new QueryTask(west, north, east, south);
        return pool.invoke(queryTask); // Invoke the query task
    }

    static class PreprocessTask extends RecursiveTask<Void> {
        private final String filename;
        private final int startIndex;
        private final int gridSize;
        private final int columns, rows, version;

        PreprocessTask(String filename, int startIndex, int gridSize, int columns, int rows, int version) {
            this.filename = filename;
            this.startIndex = startIndex;
            this.gridSize = gridSize;
            this.columns = columns;
            this.rows = rows;
            this.version = version;
        }

        @Override
        protected Void compute() {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                int currentIndex = 0;
                int totalPopulation = 0;

                reader.readLine(); // Skip header line

                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");

                    if (tokens.length >= 7) {
                        try {
                            double latitude = Double.parseDouble(tokens[LATITUDE_INDEX]);
                            double longitude = Double.parseDouble(tokens[LONGITUDE_INDEX]);
                            int population = Integer.parseInt(tokens[POPULATION_INDEX]);

                            if (longitude >= -125.0 && longitude <= -66.93457 &&
                                latitude >= 24.396308 && latitude <= 49.384358) {
                                totalPopulation += population;
                            }

                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing data: " + line);
                        }
                    }

                    currentIndex++;
                    if (currentIndex >= gridSize) break;
                }

                lock.lock();
                try {
                } finally {
                    lock.unlock();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null; // No return value needed
        }
    }

    // Query Task to calculate population
    static class QueryTask extends RecursiveTask<Long> {
        private final float west, north, east, south;

        QueryTask(float west, float north, float east, float south) {
            this.west = west;
            this.north = north;
            this.east = east;
            this.south = south;
        }

        @Override
        protected Long compute() {
            long totalPopulation = 0;

          
            lock.lock();
            try {
                totalPopulation += 1000; // Dummy population value for now
            } finally {
                lock.unlock();
            }

            return totalPopulation;
        }
    }

    public static int getColumns() {
        return 10; // Example value
    }

    public static int getRows() {
        return 10; // Example value
    }

    public static int getVersionNum() {
        return 1; // Example version
    }
}
