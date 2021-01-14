package adminTool;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipOutputStream;

import adminTool.addressIndex.IndexWriter;
import adminTool.addressIndex.StreetLocator;
import adminTool.elements.Boundary;
import adminTool.elements.Building;
import adminTool.elements.IPointAccess;
import adminTool.elements.LineLabel;
import adminTool.elements.MultiElement;
import adminTool.elements.POI;
import adminTool.elements.PointLabel;
import adminTool.elements.Street;
import adminTool.elements.Way;
import adminTool.labeling.IDrawInfo;
import adminTool.labeling.INameInfo;
import adminTool.labeling.RoadLabelCreator;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.metrics.IDistanceMap;
import adminTool.metrics.PixelToCoordDistanceMap;
import adminTool.projection.MercatorProjection;
import adminTool.projection.Projector;
import util.IntList;

public class CreateTest {
    private static int BUFFER_SIZE = 65536;

    public static void main(final String[] args) {
        new CreateTest();
    }

    public CreateTest() {

        try (ZipOutputStream zipOutput = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream("default.map"), BUFFER_SIZE))) {
            zipOutput.setLevel(ZipOutputStream.STORED);
            // Reading the osm.pbf data
            long start = System.currentTimeMillis();
            IOSMParser parser = new OSMParser();
            try {
                parser.read(new File("default.pbf"));
            } catch (final Exception e) {
                System.err.println("Fehler beim Lesen der Datei.");
                System.exit(1);
            }

            // Internal conversion of the data and writing
            final Collection<Way> ways = parser.getWays();
            final Collection<POI> pois = parser.getPOIs();
            final Collection<PointLabel> pointLabels = parser.getPointLabels();
            final Collection<Building> buildings = parser.getBuildings();
            final Collection<MultiElement> areas = parser.getAreas();
            Collection<Boundary> boundaries = parser.getBoundaries();
            IPointAccess points = parser.getPoints();
            parser = null;
            System.out.println("OSM read time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            start = System.currentTimeMillis();
            GraphWriter graphWriter = new GraphWriter(ways, points, zipOutput);
            graphWriter.write();

            final List<Street> streets = graphWriter.getStreets();
            graphWriter = null;
            System.out.println("graph creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            Projector projector = new Projector(new MercatorProjection());
            projector.performProjection(points);
            projector = null;

            MapBoundsCalculator mapBoundsCalc = new MapBoundsCalculator();
            mapBoundsCalc.calculateBounds(points, ways, areas);
            final Rectangle2D mapBounds = mapBoundsCalc.getBounds();
            mapBoundsCalc = null;

            int[] minZoomstep = new int[] { 13, 15, 9, 12, 13, 15, 15, 15, 15, 15, 10, 11, 14, 16, 7, 5, 5, 11, 11, 11,
                    17, 17, 15, 15 };

            start = System.currentTimeMillis();
            Collection<LineLabel> lineLabels = new ArrayList<LineLabel>();

            RoadLabelCreator labelCreator = new RoadLabelCreator(ways, points, mapBounds);
            for (int zoom = 0; zoom <= 19; ++zoom) {
                System.out.println("create labels for zoom " + zoom);
                final int z = zoom;
                final IDistanceMap pixelsToCoords = new PixelToCoordDistanceMap(zoom);

                labelCreator.createLabels(pixelsToCoords, new IDrawInfo() {

                    @Override
                    public double getStrokeWidth(int type) {
                        return pixelsToCoords.map(12);
                    }

                    @Override
                    public double getFontSize(int type) {
                        return pixelsToCoords.map(10);
                    }

                    @Override
                    public boolean isVisible(int type) {
                        return type < minZoomstep.length ? z >= minZoomstep[type] : true;
                    }
                }, zoom);
                points = labelCreator.getPoints();
                lineLabels.addAll(labelCreator.getLabeling());
            }

            System.out.println("label creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

            start = System.currentTimeMillis();

            MapManagerWriter mapManagerWriter = new MapManagerWriter(streets, areas, buildings, lineLabels, pois,
                    pointLabels, points, mapBounds, zipOutput);
            mapManagerWriter.write();

            System.out.println("map manager creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");
            start = System.currentTimeMillis();

            StreetLocator locator = new StreetLocator(boundaries, points, mapBounds);
            IndexWriter indexWriter = new IndexWriter(locator, mapManagerWriter.streetSorting, zipOutput);
            mapManagerWriter = null;
            indexWriter.write();

            System.out.println("index creation time: " + (System.currentTimeMillis() - start) / 1000 + "s");

        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void addRoadMap(final List<Street> streets, final List<LabelSection> roadSections,
            final List<LabelSection> junctionSections, final INameInfo info) {
        for (final LabelSection roadSection : roadSections) {
            final IntList indices = new IntList(roadSection.size());
            for (int i = 0; i < roadSection.size(); ++i) {
                indices.add(roadSection.getPoint(i));
            }
            streets.add(new Street(indices, 24,
                    "road section (" + roadSection.getRoadId() + ", " + info.getName(roadSection.getRoadId()) + ")",
                    -1));
        }
        for (final LabelSection junctionSection : junctionSections) {
            final IntList indices = new IntList(junctionSection.size());
            for (int i = 0; i < junctionSection.size(); ++i) {
                indices.add(junctionSection.getPoint(i));
            }
            streets.add(new Street(indices, 25, "junction section (" + junctionSection.getRoadId() + ", "
                    + info.getName(junctionSection.getRoadId()) + ")", -1));
        }
    }
}