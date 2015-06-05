package adminTool;

import java.io.File;

public class AdminApplication {

    private File osm;
    private File outputPath;

    public void start() {
        System.out.println("Tool zur Konvertierung von *.pbf Daten gestartet.");

        final AdminInterface admininterface = new AdminDialog();

        osm = admininterface.askOSMFile();
        outputPath = admininterface.askOutputPath();

        // Generates the filename of the outputfile
        String outFileName = osm.getName();

        if (outputPath.getName().equals("")) {
            outFileName = "default";
            outputPath = new File("default.tsk");
        } else {
            if (outFileName.endsWith(".osm.pbf")) {
                outFileName = outFileName.substring(0, outFileName.length() - 8);
            } else if (outFileName.endsWith(".pbf")) {
                outFileName = outFileName.substring(0, outFileName.length() - 4);
            }

            outFileName = outFileName.concat(".map");

            outputPath = new File(outputPath.toString() + "/" + outFileName);
        }

        System.out.println("Starte Konvertierung der Kartendaten");

        // Reading the osm.pbf data
        OSMParser parser = new OSMParser();
        try {
            parser.read(osm);
        } catch (final Exception e) {
            System.out.println("Fehler beim Lesen der Datei.");
            System.exit(1);
        }

        // Internal conversion of the data and writing

        GraphCreator graphCreator = new GraphCreator(parser.getStreets(), outputPath);
        graphCreator.create();

        MapManagerCreator mmc = new MapManagerCreator(parser.getBuildings(), graphCreator.getStreets(),
                parser.getPOIs(), parser.getWays(), parser.getTerrain(), parser.getBoundingBox(), outputPath);

        parser = null;
        graphCreator = null;

        mmc.create();

        mmc = null;
        admininterface.complete();

    }
}
