import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        // Configuration Para
        String ip = "IP";
        String dropFilePath = "LocalPath";
        String backupFilePath = "LocalPath";
        String deleteFile = "Y"; // Y | N

        // Retry para
        int retry = 3; // Retry backup
        long retryDelayMillis = 1000; // Delay in milliseconds between retries
        int readRetryCount = 3; // Number of retries for reading a file
        long readRetryDelayMillis = 1000; // Delay in milliseconds between read retries

        // WatchService monitor
        Path dirToWatch = Paths.get(dropFilePath);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            // DropFile for file creation events
            dirToWatch.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("Watching directory: " + dropFilePath);

            // Create an ExecutorService to handle file reading in the background
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            while (true) {
                WatchKey key;
                try {
                    // Wait for an event to occur (blocking call)
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.err.println("WatchService interrupted: " + e.getMessage());
                    return; // Exit if interrupted
                }

                // Process events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Check for file creation event
                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                        Path fileName = (Path) event.context();
                        File file = dirToWatch.resolve(fileName).toFile();

                        // Print the file
                        System.out.println("Contents of the file: " + file.getName());

                        // Service to handle file background
                        executorService.submit(() -> {
                            // Read the file content and display it in the console with retries
                            boolean readSuccess = false;
                            for (int i = 0; i < readRetryCount; i++) {
                                try {
                                    displayFileContents(file); // Try reading file with lock
                                    readSuccess = true;
                                    break; // Exit retry loop on success
                                } catch (IOException e) {
                                    // Do not print the error to the console in the background
                                    // Just retry silently
                                    if (i == readRetryCount - 1) {
                                        System.err.println("Error reading file " + file.getName() + " after "
                                                + readRetryCount + " attempts.");
                                    }
                                    try {
                                        Thread.sleep(readRetryDelayMillis); // Sleep 1 second before retrying
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }

                            if (!readSuccess) {
                                return; // Skip further processing if reading fails
                            }

                            // Backup the file with timestamp
                            Path sourcePath = file.toPath();

                            // Generate a unique backup filename with timestamp
                            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                            Path backupPath = Paths.get(backupFilePath, getUniqueFileName(file.getName(), timestamp));

                            // Retry mechanism for backup
                            boolean backupSuccess = false;
                            for (int i = 0; i < retry; i++) {
                                try {
                                    Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                                    backupSuccess = true;
                                    break; // Exit the retry loop on success
                                } catch (IOException e) {
                                    // Log errors only for the first attempt
                                    if (i == 0) {
                                        System.err.println("Error during backup, attempt " + (i + 1) + " of " + retry);
                                    }
                                    try {
                                        Thread.sleep(retryDelayMillis); // Sleep for 1 second before retrying
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }

                            // If backup was successful and deleteFile flag is "Y", delete the original file
                            if (backupSuccess) {
                                System.out.println("File backed up successfully.");
                                if ("Y".equalsIgnoreCase(deleteFile)) {
                                    try {
                                        Files.delete(sourcePath);
                                        System.out.println("DeleteFile successfully.");
                                    } catch (IOException e) {
                                        System.err.println("Error deleting file: " + e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                }

                // Reset the key to continue watching
                boolean valid = key.reset();
                if (!valid) {
                    System.err.println("WatchKey no longer valid, stopping the watch service.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper function to generate a unique file name by adding timestamp to avoid
    // overwrite
    private static String getUniqueFileName(String originalFileName, String timestamp) {
        // Get file extension
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
            originalFileName = originalFileName.substring(0, dotIndex);
        }

        // Generate unique filename by appending timestamp
        return originalFileName + "_" + timestamp + fileExtension;
    }

    // Helper function to display the file content in the console
    private static void displayFileContents(File file) throws IOException {
        // Retry mechanism to handle file locking by other processes
        boolean locked = false;
        int retryCount = 3;
        while (!locked && retryCount > 0) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                    FileChannel fileChannel = randomAccessFile.getChannel()) {
                // Attempt to lock the file before reading
                FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, true);
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Print the content of the file to the console (Optional)
                        System.out.println(line);
                    }
                } finally {
                    lock.release(); // Release the lock
                    locked = true; // Mark as locked successfully
                }
            } catch (IOException e) {
                retryCount--;
                if (retryCount == 0) {
                    throw new IOException("Unable to read file " + file.getName() + " after multiple retries.");
                }
                // Sleep for 1 second before retrying (1 second)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
