package model.reader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.ITextProcessor;
import model.TextProcessor;
import model.TextProcessor.Entry;
import model.map.IMapManager;
import model.reader.Reader.ReaderContext;

class TextProcessorReader {
    private ITextProcessor textProcessor;

    public TextProcessorReader() {
        super();
    }

    public ITextProcessor getTextProcessor() {
        return textProcessor;
    }

    public void readIndex(final ReaderContext readerContext, final IMapManager mapManager) throws IOException {
        final Collection<Entry> entries = readStreets(readerContext);
        textProcessor = new TextProcessor(entries, mapManager);
    }

    private Collection<Entry> readStreets(final ReaderContext readerContext) throws IOException {
        final List<TextProcessor.Entry> list = new ArrayList<>();
        final DataInputStream stream = readerContext.createInputStream("index");
        if (stream != null) {
            final String[] cities = readCities(stream);

            int maxCityCount = stream.readInt();

            for (int cityCount = 1; cityCount <= maxCityCount; ++cityCount) {
                final int n = stream.readInt();

                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < cityCount; k++) {
                        final int street = stream.readInt();
                        final int city = stream.readInt();
                        final String cityName = city != -1 ? cities[city] : "";

                        list.add(new TextProcessor.Entry(cityName, street));
                    }
                }
            }
            stream.close();
        }

        return list;
    }

    private String[] readCities(final DataInputStream stream) throws IOException {
        final String[] cities = new String[stream.readInt()];

        for (int i = 0; i < cities.length; i++) {
            cities[i] = stream.readUTF();
        }
        return cities;
    }

}