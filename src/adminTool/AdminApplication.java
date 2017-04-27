package adminTool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Boundary;

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

        System.out.println("Starte Konvertierung der Kartendaten.");

        // Reading the osm.pbf data
        OSMParser parser = new OSMParser();
        try {
            parser.read(osm);
        } catch (final Exception e) {
            System.err.println("Fehler beim Lesen der Datei.");
            System.exit(1);
        }

        // Internal conversion of the data and writing

        ZipOutputStream zipOutput = null;
        try {
            zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("default.map")));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        if (zipOutput != null) {
            GraphWriter graphWriter = new GraphWriter(parser.getStreets(), zipOutput);
            try {
                graphWriter.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            MapManagerWriter mapManagerWriter = new MapManagerWriter(parser.getBuildings(), graphWriter.getStreets(),
                    parser.getPOIs(), parser.getWays(), parser.getTerrain(), parser.getLabels(),
                    parser.getBoundingBox(), zipOutput);

            // TODO take street list of mapManagerCreator instead of graph creator
            // [already sorted]

            List<List<Boundary>> boundaries = parser.getBoundaries();
            parser = null;
            graphWriter = null;

            try {
                mapManagerWriter.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            IndexWriter indexCreator = new IndexWriter(boundaries, mapManagerWriter.streetSorting, zipOutput);
            mapManagerWriter = null;
            boundaries = null;

            try {
                indexCreator.write();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            try {
                zipOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        admininterface.complete();

    }
}
