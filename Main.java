import java.io.IOException;

public class Main{
    public static void main(String[] args) {
        // "C:\\Users\\219\\Desktop\\Python311"      C:\Users\akasi\Desktop\matura
        // "C:\\Users\\219\\Desktop\\folder"         C:\Users\akasi\Desktop\ttt
        String srcDirectory = "C:\\Users\\akasi\\Desktop\\test_to_copy"; // ścieżka źródłowa
        String destDirectory = "C:\\Users\\akasi\\Desktop\\ttt"; // ścieżka docelowa
        int nThreads = 2; // liczbą wątków
        int bufferSize = 8; // mnozona *1024 (bajtow) czyli 1 = 1KB

        try {
            MultithreadedFileCopy.copyFiles(srcDirectory, destDirectory, nThreads, bufferSize);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}


// 1 watek 1024 buff - 42sek
// 1 watek 4096 buff - 7,5 sek

// 2 watki 1024 buff - 7,5sek
// 2 watki 8 buff - 5,1 sek

// 4 watki 1024 buff - 5,3sek
// 4 watki 4096 buff - 4,6 sek