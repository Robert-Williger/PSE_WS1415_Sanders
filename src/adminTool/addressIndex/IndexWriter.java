package adminTool.addressIndex;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;
import java.util.Set;

import adminTool.AbstractMapFileWriter;
import adminTool.Sorting;
import adminTool.elements.Street;

public class IndexWriter extends AbstractMapFileWriter {
    private Sorting<Street> streets;
    private Map<String, Integer> cityMap;
    private int cityId;
    private final StreetLocator locator;

    // TODO speedup
    public IndexWriter(final StreetLocator streetLocator, final Sorting<Street> streets,
            final ZipOutputStream zipOutput) {
        super(zipOutput);

        this.cityMap = new LinkedHashMap<>();
        cityMap.put(null, -1);
        this.cityId = -1;
        this.locator = streetLocator;
        this.streets = streets;
    }

    @Override
    public void write() throws IOException {

        AssociatedStreet[][][] streets = orderStreets();

        putNextEntry("index");
        writeCities();
        writeStreets(streets);
        closeEntry();

    }

    // orderStreets(...)[i][j][k]:
    // i -> all streets existing in exactly i cities; i in [0, getMaxOccurance(...))
    // j -> j-th street within i cities
    // k -> street in k-th city with j-th street name; k in [0, i)
    private AssociatedStreet[][][] orderStreets() {

        final Map<String, Set<AssociatedStreet>> streetMap = createStreetMap();
        AssociatedStreet[][][] ret = new AssociatedStreet[getMaxOccurance(streetMap)][][];

        int[] occurances = new int[ret.length];
        for (final Entry<String, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            ++occurances[entry.getValue().size() - 1];
        }

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new AssociatedStreet[occurances[i]][];
            occurances[i] = 0;
        }

        for (final Entry<String, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            int size = entry.getValue().size();
            final AssociatedStreet[] collisions = new AssociatedStreet[size];
            int count = -1;
            for (final AssociatedStreet street : entry.getValue()) {
                collisions[++count] = street;
            }
            ret[size - 1][occurances[size - 1]] = collisions;
            ++occurances[size - 1];
        }

        return ret;
    }

    private int getMaxOccurance(final Map<String, Set<AssociatedStreet>> streetMap) {
        int max = 0;
        for (final Entry<?, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            max = Math.max(max, entry.getValue().size());
        }
        return max;
    }

    private Map<String, Set<AssociatedStreet>> createStreetMap() {
        final Map<String, Set<AssociatedStreet>> streetMap = new HashMap<>();

        int streetId = 0;
        for (final Street street : streets.elements) {
            final String name = street.getName();
            if (name != null) {
                final String city = locator.getCity(street);
                final int cityId = generateCityId(city);

                Set<AssociatedStreet> streetSet = streetMap.get(name);
                if (streetSet == null) {
                    streetSet = new HashSet<>();
                    streetMap.put(name, streetSet);
                }
                streetSet.add(new AssociatedStreet(streetId, cityId));
            }
            ++streetId;
        }

        return streetMap;
    }

    private int generateCityId(final String city) {
        Integer id = cityMap.get(city);
        if (id == null) {
            id = ++this.cityId;
            cityMap.put(city, id);
        }

        return id;
    }

    private void writeStreets(final AssociatedStreet[][][] streets) throws IOException {
        dataOutput.writeInt(streets.length);
        for (int i = 0; i < streets.length; i++) {
            dataOutput.writeInt(streets[i].length);
            for (int j = 0; j < streets[i].length; j++) {
                for (int k = 0; k < i + 1; k++) {
                    final AssociatedStreet street = streets[i][j][k];
                    dataOutput.writeInt(street.streetId);
                    dataOutput.writeInt(street.cityId);
                }
            }
        }
    }

    private void writeCities() throws IOException {
        cityMap.remove(null);
        dataOutput.writeInt(cityMap.size());
        for (final Entry<String, Integer> entry : cityMap.entrySet()) {
            dataOutput.writeUTF(entry.getKey());
        }
    }

    private static class AssociatedStreet implements Comparable<AssociatedStreet> {
        private final int streetId;
        private final int cityId;

        public AssociatedStreet(final int streetId, final int cityId) {
            this.streetId = streetId;
            this.cityId = cityId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + cityId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AssociatedStreet other = (AssociatedStreet) obj;
            if (cityId != other.cityId) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(final AssociatedStreet o) {
            return streetId - o.streetId;
        }
    }
}
