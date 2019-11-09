package mindisco;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class BanList {
    public ArrayList<Long> bans;
    public File file;
    public BanList (File file) throws IOException {
        this.file = file;
        FileInputStream stream = new FileInputStream(file);

        long id = 0;
        int i = 0;
        int read = stream.read();
        while(read > 0){

            id <<= 8;
            id |= (stream.read() & 0xFF);
        }
        stream.close();
    }

}
