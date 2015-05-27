package adminTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Encapsulates the dialogues.
 */
public class AdminDialog implements AdminInterface {

    @Override
    public File askOSMFile() {

        System.out.println("Pfad der Kartendaten angeben:");

        final String path = readConsole();

        File inputFile;

        inputFile = new File(path);

        if (path.equals("exit")) {
            System.exit(0);
        }

        if (!inputFile.exists() || inputFile.isDirectory() || !inputFile.getName().endsWith(".pbf")) {
            System.out.println("Pfadangabe fehlerhaft. 'exit' eingeben, um das Programm zu beenden");

            return askOSMFile();

        }

        return inputFile;
    }

    @Override
    public File askOutputPath() {

        System.out.println("Zielverzeichnis angeben:");

        final String path = readConsole();

        if (path.equals("")) {
            return new File("");
        }

        final File outputFile = new File(path);

        if (path.equals("exit")) {
            System.exit(0);
        }
        if (!outputFile.exists() || outputFile.isFile()) {
            System.out.println("Pfadangabe fehlerhaft. 'exit' eingeben, um das Programm zu beenden");

            return askOutputPath();

        }

        return outputFile;

    }

    @Override
    public void complete() {
        System.out.println("Konvertierung abgeschlossen. Zum beenden 'Enter' drücken.");
        // arbitrary input ended with enter
        readConsole();

    }

    private String readConsole() {

        final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        String path = null;

        try {
            path = console.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return path;
    }

}