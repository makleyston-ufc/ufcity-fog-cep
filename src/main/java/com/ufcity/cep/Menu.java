package com.ufcity.cep;

import com.ufcity.cep.storage.MongoDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static ufcitycore.mqtt.ConnectionData.*;

public class Menu {
    public static int check(String[] params){
        String da = null, dp="27017";
        int qtArgs = params.length;
        if(qtArgs == 0) {
            System.out.println("Invalid parameters. Type -h (or --help) for help.");
            return 1;
        }
        if(qtArgs == 1){
            if(params[0].equals("-h") || params[0].equals("--help")){
                System.out.println("-fa \t--fog-address         \tAddress to fog computing.");
                System.out.println("-ca \t--cloud-address       \tAddress to cloud computing.");
                System.out.println("-fp \t--fog-port            \tPort to edge computing.");
                System.out.println("-cp \t--cloud-port          \tPort to cloud computing.");
                System.out.println("-da \t--database-address    \tAddress to database.");
                System.out.println("-dp \t--database-port       \tPort to database");
                System.out.println("-v  \t--version             \tVersion of this system.");
            } else if (params[0].equals("-v") || params[0].equals("--version")) {
                System.out.println("Version: " + Main.version);
            } else {
                System.out.println("Invalid parameters. Type -h (or --help) for help.");
            }
            return 1;
        }
        if(qtArgs % 2 != 0){
            System.out.println("Invalid parameters. Type -h (or --help) for help.");
            return 1;
        }else{
            int i = 0;
            while (i < qtArgs){
                switch (params[i]) {
                    case "-fa", "--fog-address" -> setInnerHost(params[i + 1]);
                    case "-ca", "--cloud-address" -> setHostCloud(params[i + 1]);
                    case "-fp", "--fog-port" -> setInnerPort(params[i + 1]);
                    case "-cp", "--cloud-port" -> setPortCloud(params[i + 1]);
                    case "-da", "--database-address" -> da = params[i + 1];
                    case "-dp", "--database-port" -> dp = params[i + 1];
                }
                i = i + 2;
            }
            if(da != null) {
                System.out.println(">> Connecting database! Database address: "+da+":"+dp);
                Main.database = new MongoDB(da, dp);
            }
            return 0;
        }
    }

    public static String[] ReaderConfig() throws IOException {
        String path = new File("ufcity-cep.config").getAbsolutePath();
//        System.out.println(path);
        BufferedReader buffRead = new BufferedReader(new FileReader(path));
        List<String> args = new ArrayList<>();
        String line = "";
        while (true) {
            line = buffRead.readLine();
            if (line != null) {
                String[] l = line.split(":");
                args.add(l[0].trim());
                args.add(l[1].trim());
//                System.out.println(l[0] + " ## " + l[1]);
            } else {
                buffRead.close();
                System.out.println(Arrays.toString(args.toArray(new String[0])));
                return args.toArray(new String[0]);
            }
        }
    }

}
