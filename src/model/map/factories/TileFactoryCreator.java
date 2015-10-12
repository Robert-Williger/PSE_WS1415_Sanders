package model.map.factories;

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
            writer = new BufferedWriter(new FileWriter(new File("src/model/map/factories/StorageTileFactory.java")));
            createClass();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createClass() throws IOException {
        int classes = 1 << elements.length;

        writer.write("package model.map.factories;");
        writer.newLine();
        writer.newLine();
        writeImports();
        writer.newLine();
        writer.write("public class StorageTileFactory extends AbstractTileFactory implements ITileFactory {");
        writer.newLine();
        writer.newLine();
        writer.write("private final ITileFactory[] factories;");
        writer.newLine();
        writer.newLine();
        writer.write("public StorageTileFactory(final CompressedInputStream reader");
        for (int i = 0; i < elements.length; i++) {
            writer.write(", " + elements[i] + "[] " + fields[i]);
        }
        writer.write(") {");
        writer.newLine();
        writer.write("super(reader");
        for (final String field : fields) {
            writer.write(", " + field);
        }
        writer.write(");");
        writer.newLine();
        writer.newLine();
        writer.newLine();
        writer.write("factories = new ITileFactory[" + classes + "];");
        writer.newLine();
        writer.write("for (int i = 0; i < " + classes + "; i++) {");
        writer.newLine();
        for (int i = 0; i < classes; i++) {
            writer.write("factories[" + i + "] = new TileFactory" + i + "();");
            writer.newLine();
        }
        writer.write("}");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.newLine();
        writer.write("@Override");
        writer.newLine();
        writer.write("public ITile createTile(int row, int column, int zoom) throws IOException {");
        writer.newLine();
        writer.write("return factories[reader.readByte()].createTile(zoom, row, column);");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.newLine();

        createEmptyTileFactory();
        writer.newLine();

        for (int i = 1; i < classes; i++) {
            createTileFactory(i);
            writer.newLine();
            writer.newLine();
            createTileClass(i);
            writer.newLine();
            writer.newLine();
        }
        writer.write("}");
    }

    private void writeImports() throws IOException {
        writer.write("import java.util.Iterator;");
        writer.newLine();
        writer.write("import java.io.IOException;");
        writer.newLine();
        writer.newLine();
        writer.write("import util.Arrays;");
        writer.newLine();
        writer.write("import model.CompressedInputStream;");
        writer.newLine();
        writer.write("import model.elements.Label;");
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
        writer.newLine();
        writer.write("import model.map.AbstractTile;");
        writer.newLine();
        writer.write("import model.map.ITile;");
        writer.newLine();
    }

    private void createFactoryHeader() throws IOException {
        writer.write("ITile createTile(int zoom, int row, int column) throws IOException");
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
        writer.write("return EMPTY_TILE;");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.write("}");
    }

    private void createTileFactory(final int flags) throws IOException {
        boolean[] existing = new boolean[elements.length];

        for (int i = 0; i < fields.length; i++) {
            existing[i] = ((flags >> i & 1) == 1);
        }

        writer.write("private class TileFactory" + flags + " implements ITileFactory {");
        writer.newLine();
        writer.newLine();
        writer.write("@Override");
        writer.newLine();
        writer.write("public ");
        createFactoryHeader();
        writer.write(" {");
        writer.newLine();
        for (int i = 0; i < elements.length; i++) {
            if (existing[i]) {
                writer.write(elements[i] + "[] tile" + fields[i] + " = new " + elements[i]
                        + "[reader.readCompressedInt()];");
                writer.newLine();
                writer.write("fillElements(" + fields[i] + ", tile" + fields[i] + ");");
                writer.newLine();
            }
        }
        writer.write("return new Tile" + flags + "(");
        for (int i = 0; i < elements.length; i++) {
            if (existing[i]) {
                writer.write("tile" + fields[i] + ", ");
            }
        }
        writer.write("zoom, row, column);");
        writer.newLine();
        writer.write("}");
        writer.newLine();
        writer.write("}");
    }

    private void createTileClass(final int flags) throws IOException {
        boolean[] existing = new boolean[elements.length];
        for (int i = 0; i < fields.length; i++) {
            existing[i] = ((flags >> i & 1) == 1);
        }

        writer.write("private static class Tile" + flags + " extends AbstractTile {");
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

        writer.write("public Tile" + flags + "(");
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
        writer.newLine();
    }

    static {
        elements = new String[]{"POI", "Street", "Way", "Building", "Area", "Label"};
        fields = new String[]{"pois", "streets", "ways", "buildings", "areas", "labels"};
        methods = new String[]{"getPOIs()", "getStreets()", "getWays()", "getBuildings()", "getTerrain()",
                "getLabels()"};
    }
}
