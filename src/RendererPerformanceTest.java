import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.util.Iterator;

import javafx.scene.shape.Rectangle;

import com.sun.javafx.geom.Line2D;

import model.IProgressListener;
import model.Reader;
import model.elements.IStreet;
import model.map.IPixelConverter;
import model.map.ITile;
import model.renderEngine.BackgroundRenderer;

public class RendererPerformanceTest {

    public static void main(String[] args) {
        Reader reader = new Reader();
        reader.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(int progress) {
                System.out.println("Lese Karte ein: " + progress + " %.");
            }

            @Override
            public void stepCommenced(String step) {

            }

            @Override
            public void errorOccured(String message) {

            }

        });
        reader.read(new File("default.map"));
        final ITile tile = reader.getMapManager().getTile(5188147241029730658L);
        final IPixelConverter converter = reader.getMapManager().getConverter();

        final int zoom = tile.getZoomStep();

        final int tileSize = converter.getCoordDistance(256, zoom);

        final int row = tile.getRow();
        final int column = tile.getColumn();

        final Line2D[] boundingLines = new Line2D[4];
        boundingLines[0] = new Line2D(column * tileSize, row * tileSize, (column + 1) * tileSize, row * tileSize);
        boundingLines[1] = new Line2D(column * tileSize, row * tileSize, column * tileSize, (row + 1) * tileSize);
        boundingLines[2] = new Line2D(column * tileSize, (row + 1) * tileSize, (column + 1) * tileSize, (row + 1)
                * tileSize);
        boundingLines[3] = new Line2D(column * tileSize, row * tileSize, (column + 1) * tileSize, (row + 1) * tileSize);

        final Rectangle bounds = new Rectangle(column * tileSize, row * tileSize, (column + 1) * tileSize, (row + 1)
                * tileSize);

        final Line2D line = new Line2D();
        for (final Iterator<IStreet> iterator = tile.getStreets(); iterator.hasNext();) {
            final IStreet street = iterator.next();
            for (int i = 0; i < street.getLength(); i++) {
                int lastX = street.getX(0);
                int lastY = street.getY(0);
                boolean lastInside = bounds.contains(lastX, lastY);

                for (int j = 1; j < street.getLength(); j++) {
                    int x = street.getX(j);
                    int y = street.getY(j);

                    if (lastInside && !bounds.contains(x, y)) {
                        ;
                    }
                    line.setLine(lastX, lastY, x, y);

                    boolean intersection = false;
                    for (final Line2D bound : boundingLines) {
                        if (line.intersectsLine(bound)) {
                            intersection = true;
                            break;
                        }
                    }

                    if (intersection) {

                    }
                    lastX = x;
                    lastY = y;
                }
            }
        }
        new BackgroundRenderer(reader.getMapManager().getConverter());
    }
}
