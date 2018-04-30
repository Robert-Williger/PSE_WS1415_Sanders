package model.reader;

import java.io.DataInputStream;
import java.io.IOException;

import model.map.IMapManager;
import model.reader.Reader.ReaderContext;
import model.routing.DirectedGraph;
import model.routing.IDirectedGraph;
import model.routing.IRouteManager;
import model.routing.RouteManager;

class RouteManagerReader {
    private IRouteManager routeManager;

    public RouteManagerReader() {
        super();
    }

    public IRouteManager getRouteManager() {
        return routeManager;
    }

    public void readRouteManager(final ReaderContext readerContext, final IMapManager mapManager) throws IOException {
        final IDirectedGraph graph = readGraph(readerContext, mapManager);
        routeManager = new RouteManager(graph, mapManager);
    }

    private IDirectedGraph readGraph(final ReaderContext readerContext, final IMapManager mapManager)
            throws IOException {
        final DataInputStream stream = readerContext.createInputStream("graph");
        if (stream != null) {
            final int nodeCount = stream.readInt();
            final int edgeCount = stream.readInt();
            final int onewayCount = stream.readInt();

            final int[] firstNodes = new int[edgeCount];
            final int[] secondNodes = new int[edgeCount];
            final int[] weights = new int[edgeCount];

            for (int i = 0; i < edgeCount; i++) {
                firstNodes[i] = stream.readInt();
                secondNodes[i] = stream.readInt();
                weights[i] = stream.readInt();
            }

            stream.close();

            return new DirectedGraph(nodeCount, onewayCount, firstNodes, secondNodes, weights);
        }

        return new DirectedGraph(0, 0, new int[0], new int[0], new int[0]);
    }

}