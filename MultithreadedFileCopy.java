import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class MultithreadedFileCopy {

    private static JProgressBar overallProgressBar;

    private static ArrayList<JProgressBar> initProgressBars(int nThreads) {
        ArrayList<JProgressBar> bars = new ArrayList<>();
        JFrame progressBarFrame = new JFrame("Progress Bars");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        progressBarFrame.add(panel);
        progressBarFrame.setSize(500, 500);
        progressBarFrame.setVisible(true);
        progressBarFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        for (int i = 0; i < nThreads; i++) {
            JProgressBar progressBar = new JProgressBar();
            bars.add(progressBar);
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setStringPainted(true);
            panel.add(progressBar);
        }

        panel.revalidate();
        panel.repaint();
        return bars;
    }

    private static void initOverallProgressBar(long totalSize) {
        JFrame overallProgressBarFrame = new JFrame("Overall Progress");
        JPanel overallPanel = new JPanel();
        overallProgressBar = new JProgressBar();
        overallProgressBar.setMinimum(0);
        overallProgressBar.setMaximum((int) totalSize);
        overallProgressBar.setStringPainted(true);
        overallPanel.add(overallProgressBar);
        overallProgressBarFrame.add(overallPanel);
        overallProgressBarFrame.setSize(500, 100);
        overallProgressBarFrame.setVisible(true);
        overallProgressBarFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static void copyFileUsingStream(File source, File dest, int buffSize, JProgressBar progressBar, AtomicLong overallProgress) throws IOException {
        try (InputStream is = Files.newInputStream(source.toPath()); OutputStream os = Files.newOutputStream(dest.toPath())) {
            byte[] buffer = new byte[buffSize * 1024];
            int length;
            long totalBytesRead = 0;
            long fileSize = source.length();

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                totalBytesRead += length;
                int progress = (int) (totalBytesRead * 100 / fileSize);
                progressBar.setValue(progress);
                overallProgress.addAndGet(length);
                overallProgressBar.setValue(overallProgress.intValue());
            }
        }
    }

    public static void copyFiles(String srcDirectory, String destDirectory, int nThreads, int buffSize) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        File dir = new File(srcDirectory);
        long totalSize = folderSize(dir);
        initOverallProgressBar(totalSize);
        ArrayList<JProgressBar> progressBars = initProgressBars(nThreads);
        AtomicLong overallProgress = new AtomicLong(0);
        BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
        try (Stream<Path> paths = Files.walk(Paths.get(srcDirectory))) {
            paths.filter(Files::isRegularFile).forEach(fileQueue::offer);
        }

        for (int i = 0; i < nThreads; i++) {
            executor.submit(() -> {
                try {
                    while (!fileQueue.isEmpty()) {
                        Path srcPath = fileQueue.poll();
                        if (srcPath != null) {
                            Path relativePath = Paths.get(srcDirectory).relativize(srcPath);
                            Path destPath = Paths.get(destDirectory).resolve(relativePath);
                            Files.createDirectories(destPath.getParent());
                            copyFileUsingStream(srcPath.toFile(), destPath.toFile(), buffSize, progressBars.get((int) (overallProgress.get() % nThreads)), overallProgress);
                            System.out.println("Skopiowano plik: " + srcPath + " do " + destPath);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        System.out.println("Wszystkie pliki zostały skopiowane.");
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }
}