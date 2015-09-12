import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TileFactoryCreator {

    private static final String[] elements;
    private static final String[] methods;
    private static final String[] fields;

    private BufferedWriter writer;

    public static void main(String[] args) {
        new TileFactoryCreator();
    }

    private TileFactoryCreator() {
        try {
            writer = new BufferedWriter(new FileWriter(new File("TileFactory.txt")));
            createClass();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createClass() throws IOException {
        writer.write("package model.map;");
        writer.newLine();
        writer.newLine();
        writeImports();
        writer.write("public class TileFactory {");
        writer.newLine();
        writer.newLine();
        writer.write("private final ITileFactory[] factories;");
        writer.newLine();
        writer.newLine();
        writer.write("public TileFactory() {");
        writer.newLine();
        writer.write("factories = new ITileFactory[32];");
        writer.newLine();
        writer.write("for (int i = 0; i < 32; i++) {");
        writer.newLine();
        for (int i = 0; i < 32; i++) {
            writer.write("factories[" + i + "] = new TileFactory" + i + "();");
            writer.newLine();
        }
        writer.write("}");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.newLine();
        writer.write("public ITile create(final byte flags, ");
        for (int i = 0; i < 5; i++) {
            writer.write("final " + elements[i] + "[] " + fields[i] + ", ");
        }
        writer.write("final int zoom, final int row, final int column) {");
        writer.newLine();
        writer.write("return factories[flags].create(");
        for (int i = 0; i < 5; i++) {
            writer.write(fields[i] + ", ");
        }
        writer.write("zoom, row, column);");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.newLine();
        createITileFactory();
        writer.newLine();
        writer.newLine();

        createEmptyTileFactory();
        writer.newLine();
        for (int i = 1; i < 32; i++) {
            createTileFactory(i);
            writer.newLine();
            writer.newLine();
        }
        writer.write("}");
    }

    private void writeImports() throws IOException {
        writer.write("import java.util.Iterator;");
        writer.newLine();
        writer.newLine();
        writer.write("import util.Arrays;");
        writer.newLine();
        writer.write("import model.elements.Area;");
        writer.newLine();
        writer.write("import model.elements.Building;");
        writer.newLine();
        writer.write("import model.elements.POI;");
        writer.newLine();
        writer.write("import model.elements.Street;");
        writer.newLine();
        writer.write("import model.elements.Way;");
        writer.newLine();
    }

    private void createITileFactory() throws IOException {
        writer.write("private static interface ITileFactory {");
        writer.newLine();
        createFactoryHeader();
        writer.write(";");
        writer.newLine();
        writer.write("}");
    }

    private void createFactoryHeader() throws IOException {
        writer.write("ITile create(");
        for (int i = 0; i < 5; i++) {
            writer.write(elements[i] + "[] " + fields[i] + ", ");
        }
        writer.write("int zoom, int row, int column)");
    }

    private void createEmptyTileFactory() throws IOException {
        writer.write("private static class TileFactory0 implements ITileFactory {");
        writer.newLine();
        writer.write("@Override");
        writer.newLine();
        writer.write("public ");
        createFactoryHeader();
        writer.write(" {");
        writer.newLine();
        writer.write("return new EmptyTile(zoom, row, column);");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.write("}");
    }

    private void createTileFactory(final int flags) throws IOException {
        boolean[] existing = new boolean[5];

        for (int i = 0; i < fields.length; i++) {
            existing[i] = ((flags >> i & 1) == 1);
        }

        writer.write("private static class TileFactory" + flags + " implements ITileFactory {");
        writer.newLine();
        writer.newLine();
        writer.write("@Override");
        writer.newLine();
        writer.write("public ");
        createFactoryHeader();
        writer.write(" {");
        writer.newLine();
        writer.write("return new Tile(");
        for (int i = 0; i < 5; i++) {
            if (existing[i]) {
                writer.write(fields[i] + ", ");
            }
        }
        writer.write("zoom, row, column);");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.newLine();
        createTileClass(flags);
        writer.newLine();
        writer.write("}");
    }

    private void createTileClass(final int flags) throws IOException {
        boolean[] existing = new boolean[5];
        for (int i = 0; i < fields.length; i++) {
            existing[i] = ((flags >> i & 1) == 1);
        }

        writer.write("private static class Tile extends AbstractTile {");
        writer.newLine();
        writer.newLine();

        for (int i = 0; i < fields.length; i++) {
            if (existing[i]) {
                writer.write("private final " + elements[i] + "[] " + fields[i] + ";");
                writer.newLine();
            }
        }
        writer.newLine();
        writer.newLine();

        writer.write("public Tile(");
        for (int i = 0; i < fields.length; i++) {
            if (existing[i]) {
                writer.write("final " + elements[i] + "[] " + fields[i] + ", ");
            }
        }
        writer.write("final int zoom, final int row, final int column) {");
        writer.newLine();
        writer.write("super(zoom, row, column);");
        writer.newLine();
        for (int i = 0; i < fields.length; i++) {
            if (existing[i]) {
                writer.write("this." + fields[i] + " = " + fields[i] + ";");
                writer.newLine();
            }
        }
        writer.write("}");
        writer.newLine();
        writer.newLine();

        for (int i = 0; i < fields.length; i++) {
            writer.newLine();
            writer.write("@Override");
            writer.newLine();
            writer.write("public Iterator<" + elements[i] + "> " + methods[i] + " {");
            writer.newLine();
            writer.write("return Arrays.iterator(" + (existing[i] ? fields[i] : "") + ");");
            writer.newLine();
            writer.write("}");
            writer.newLine();
        }

        writer.write("}");
    }

    static {
        elements = new String[]{"POI", "Street", "Way", "Building", "Area"};
        fields = new String[]{"pois", "streets", "ways", "buildings", "areas"};
        methods = new String[]{"getPOIs()", "getStreets()", "getWays()", "getBuildings()", "getTerrain()"};
    }
}
