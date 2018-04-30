package model;

import java.io.File;

import javax.swing.SwingUtilities;

import model.map.IMap;
import model.map.IMapManager;
import model.map.Map;
import model.map.MapManager;
import model.reader.Reader;
import model.renderEngine.DefaultImageLoader;
import model.renderEngine.IImageLoader;
import model.routing.DirectedGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

public class Application extends AbstractModel implements IApplication {

    private final IReader reader;
    private IRouteManager routing;
    private IMap map;
    private String name;
    private final IImageLoader loader;
    private ITextProcessor processor;

    public Application() {
        reader = new Reader();

        final IMapManager manager = new MapManager();
        loader = new DefaultImageLoader(manager);
        routing = new RouteManager(new DirectedGraph(), manager);
        processor = new TextProcessor();
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
        if (reader.read(file)) {
            routing = reader.getRouteManager();
            processor = reader.getTextProcessor();
            map = new Map(reader.getMapManager());
            loader.setMapManager(reader.getMapManager());
            name = file.getName();
            SwingUtilities.invokeLater(() -> {
                fireChange();
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

    @Override
    public String getName() {
        return name;
    }
}