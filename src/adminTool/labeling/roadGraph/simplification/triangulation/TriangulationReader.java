package adminTool.labeling.roadGraph.simplification.triangulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TriangulationReader {
    private final String name;
    private Triangulation triangulation;

    public TriangulationReader(final String filePath) {
        this.name = filePath;
    }

    public void read() throws IOException {
        final BufferedReader nodeReader = new BufferedReader(new FileReader(name + ".1.node"));
        final BufferedReader eleReader = new BufferedReader(new FileReader(name + ".1.ele"));
        final BufferedReader neighReader = new BufferedReader(new FileReader(name + ".1.neigh"));

        String[] data = nodeReader.readLine().split(" ");
        final int nodes = Integer.parseInt(data[0]);
        data = eleReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
        triangulation = new Triangulation(Integer.parseInt(data[0]), nodes);

        readNeighbors(neighReader);
        readNodes(nodeReader);
        readTriangles(eleReader);
    }

    public Triangulation getTriangulation() {
        return triangulation;
    }

    private void readNodes(final BufferedReader nodeReader) throws IOException {
        for (int i = 0; i < triangulation.size(); ++i) {
            String[] data = nodeReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            triangulation.setPoint(i, (int) Double.parseDouble(data[1]), (int) Double.parseDouble(data[2]));
        }
        nodeReader.close();
    }

    private void readTriangles(final BufferedReader eleReader) throws IOException {
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            String[] data = eleReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            triangulation.setPoints(i, parseIndex(data[1]), parseIndex(data[2]), parseIndex(data[3]));
        }
        eleReader.close();
    }

    private void readNeighbors(final BufferedReader neighReader) throws IOException {
        String[] data = neighReader.readLine().replaceAll("\\s+", " ").split(" ");
        for (int i = 0; i < triangulation.getTriangles(); ++i) {
            data = neighReader.readLine().trim().replaceAll("\\s+", " ").split(" ");
            triangulation.setNeighbors(i, parseIndex(data[1]), parseIndex(data[2]), parseIndex(data[3]));
        }
        neighReader.close();
    }

    private static int parseIndex(final String neighbor) {
        return Math.max(-1, Integer.parseInt(neighbor) - 1);
    }

}
