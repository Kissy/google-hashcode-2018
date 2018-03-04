package fun.google.hash_code_2018;


import fun.google.hash_code_2018.file_parser.ReadFile;
import fun.google.hash_code_2018.file_parser.WriteFile;
import fun.google.hash_code_2018.model.Maps;

import java.util.Map;

public class main {


    public static void main(String[] args) throws Exception {
        long timeStart = System.currentTimeMillis();
        Map<String, Maps> stringObjectMap = ReadFile.getFileFromPath();
        WriteFile.writeFileToPath(stringObjectMap);
        System.out.println((System.currentTimeMillis() - timeStart) / 1000.0 + " seconds in total");
    }
}
