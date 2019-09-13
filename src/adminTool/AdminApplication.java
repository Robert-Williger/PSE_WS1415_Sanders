package adminTool;

import java.awt.geom.Dimension2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.LineLabel;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.PointLabel;
import adminTool.elements.Street;
import adminTool.elements.Way;
import adminTool.labeling.RoadLabelCreator;
import adminTool.metrics.IDistanceMap;
import adminTool.metrics.PixelToCoordDistanceMap;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;

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
            outputPath = new File("default.map");
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
            zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        if (zipOutput != null) {
            final Collection<Way> ways = parser.getWays();
            final Collection<POI> pois = parser.getPOIs();
            final Collection<PointLabel> pointLabels = parser.getPointLabels();
            final Collection<Building> buildings = parser.getBuildings();
            final Collection<MultiElement> areas = parser.getAreas();
            IPointAccess points = parser.getPoints();
            parser = null;

            GraphWriter graphWriter = new GraphWriter(ways, points, zipOutput);
            try {
                graphWriter.write();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final List<Street> streets = graphWriter.getStreets();
            graphWriter = null;

            Projector projector = new Projector(new MercatorProjection());
            projector.performProjection(points);
            projector = null;

            MapBoundsCalculator aligner = new MapBoundsCalculator();
            aligner.calculateBounds(points, ways, areas);
            final Dimension2D mapSize = aligner.getSize();
            aligner = null;

            // RoadLabelCreator labelCreator = new RoadLabelCreator(ways, points, mapSize);
            // final int zoom = 17;
            // final IDistanceMap pixelsToCoords = new PixelToCoordDistanceMap(zoom);
            // labelCreator.createLabels(pixelsToCoords, zoom);
            // points = labelCreator.getPoints();

            Collection<LineLabel> lineLabels = new ArrayList<>();// labelCreator.getLabeling();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, areas, buildings, lineLabels, pois,
                    pointLabels, points, mapSize, zipOutput);

            try {
                mapManagerWriter.write();
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
