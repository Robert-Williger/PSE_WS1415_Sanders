package model;

import java.io.File;

import javax.swing.SwingUtilities;

import model.elements.Label;
import model.map.IMap;
import model.map.IMapManager;
import model.map.Map;
import model.map.MapManager;
import model.renderEngine.IImageLoader;
import model.renderEngine.ImageLoader;
import model.routing.Graph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Application extends AbstractModel implements IApplication {

    private final IReader reader;
    private IRouteManager routing;
    private IMap map;
    private final IImageLoader loader;
    private ITextProcessor processor;

    public Application() {
        reader = new Reader();

        final IMapManager manager = new MapManager();
        loader = new ImageLoader(manager);
        routing = new RouteManager(new Graph(0, new long[0], new int[0]), manager);
        processor = new AdvancedTextProcessor(new TextProcessor.Entry[0][], new Label[0], manager, 0);
        map = new Map(manager);
    }

    @Override
    public IMap getMap() {
        return map;
    }

    @Override
    public IRouteManager getRouteManager() {
        return routing;
    }

    @Override
    public ITextProcessor getTextProcessing() {
        return processor;
    }

    @Override
    public IImageLoader getImageLoader() {
        return loader;
    }

    @Override
    public boolean setMapData(final File file) {
        if (file != null && file.exists() && reader.read(file)) {
            routing = reader.getRouteManager();
            processor = reader.getTextProcessor();
            map = new Map(reader.getMapManager());
            loader.setMapManager(reader.getMapManager());

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fireChange();
                }

            });

            return true;
        }

        return false;
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        reader.addProgressListener(listener);
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        reader.removeProgressListener(listener);
    }

    @Override
    public void cancelCalculation() {
        reader.cancelCalculation();
    }
}