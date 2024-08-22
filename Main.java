import java.io.IOException;

public class Main{
    public static void main(String[] args) {
        String srcDirectory = "C:\\source\\path";
        String destDirectory = "C:\\destination\\path";
        int nThreads = 2; 
        int bufferSize = 8; // multiplied by 1024 = 1KB

        try {
            MultithreadedFileCopy.copyFiles(srcDirectory, destDirectory, nThreads, bufferSize);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}

// times:

// 1 thread 1024 buff - 42sek
// 1 thread 4096 buff - 7,5 sek

// 2 threads 1024 buff - 7,5sek
// 2 threads 8 buff - 5,1 sek

// 4 threads 1024 buff - 5,3sek
// 4 thread 4096 buff - 4,6 sek
