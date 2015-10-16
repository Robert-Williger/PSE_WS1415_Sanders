package adminTool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import adminTool.elements.Boundary;
import adminTool.elements.Node;
import adminTool.elements.Street;

public class IndexCreator extends AbstractMapCreator {

    private Collection<Street> streets;
    private List<List<Boundary>> boundaries;
    private Map<String, Integer> cityMap;
    private Map<String, Set<AssociatedStreet>> streetMap;
    private int id;

    public IndexCreator(final List<List<Boundary>> boundaries, final Collection<Street> streets, final File file) {
        super(file);
        this.streets = streets;
        this.boundaries = boundaries;
        this.cityMap = new LinkedHashMap<String, Integer>();
        this.streetMap = new HashMap<String, Set<AssociatedStreet>>();
        this.id = -1;
    }

    @Override
    public void create() {
        createStreetMap();

        int maxCollisions = getMaxCollision();

        AssociatedStreet[][][] streets = new AssociatedStreet[maxCollisions][][];

        fillStreets(streets);
        sortStreets(streets);

        try {
            createOutputStream(true);

            writeCities();
            writeStreets(streets);

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortStreets(final AssociatedStreet[][][] streets) {
        for (int i = 0; i < streets.length; i++) {
            AssociatedStreet[][] firstLevel = streets[i];
            for (int j = 0; j < firstLevel.length; j++) {
                AssociatedStreet[] secondLevel = firstLevel[j];
                Arrays.sort(secondLevel);
            }
            Arrays.sort(firstLevel, new Comparator<AssociatedStreet[]>() {
                @Override
                public int compare(final AssociatedStreet[] o1, final AssociatedStreet[] o2) {
                    return o1[0].streetId - o2[0].streetId;
                }
            });

        }
    }

    private void fillStreets(final AssociatedStreet[][][] streets) {
        int[] occurances = new int[streets.length];
        for (final Entry<String, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            ++occurances[entry.getValue().size() - 1];
        }

        for (int i = 0; i < streets.length; i++) {
            streets[i] = new AssociatedStreet[occurances[i]][];
            occurances[i] = 0;
        }

        for (final Entry<String, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            int size = entry.getValue().size();
            AssociatedStreet[] collisions = new AssociatedStreet[size];
            int count = -1;
            for (final AssociatedStreet street : entry.getValue()) {
                collisions[++count] = street;
            }
            streets[size - 1][occurances[size - 1]] = collisions;
            ++occurances[size - 1];
        }
    }

    private int getMaxCollision() {
        int max = 0;
        for (final Entry<String, Set<AssociatedStreet>> entry : streetMap.entrySet()) {
            if (entry.getValue().size() > max) {
                max = entry.getValue().size();
            }
        }
        return max;
    }

    private void writeStreets(final AssociatedStreet[][][] streets) throws IOException {
        writeCompressedInt(streets.length);
        for (int i = 0; i < streets.length; i++) {
            AssociatedStreet[][] firstLevel = streets[i];
            writeCompressedInt(firstLevel.length);
            int firstLevelLast = 0;
            for (int j = 0; j < firstLevel.length; j++) {
                AssociatedStreet[] secondLevel = firstLevel[j];
                int secondLevelLast = firstLevelLast;
                firstLevelLast = secondLevel[0].streetId;

                for (int k = 0; k < i + 1; k++) {
                    AssociatedStreet street = secondLevel[k];
                    writeCompressedInt(street.streetId - secondLevelLast);
                    writeCompressedInt(street.cityId);
                    secondLevelLast = street.streetId;
                }
            }
        }
    }

    private void createStreetMap() {
        int streetId = 0;
        for (final Street street : streets) {
            if (!street.getName().isEmpty()) {
                final String city = getCity(street.getNodes());
                final int cityId = generateId(city);

                Set<AssociatedStreet> streetSet = streetMap.get(street.getName());
                if (streetSet == null) {
                    streetSet = new HashSet<AssociatedStreet>();
                    streetMap.put(street.getName(), streetSet);
                }
                streetSet.add(new AssociatedStreet(streetId, cityId));
            }
            ++streetId;
        }
    }

    private int generateId(final String city) {
        Integer id = cityMap.get(city);
        if (id == null) {
            id = ++this.id;
            cityMap.put(city, id);
        }

        return id;
    }

    private String getCity(Node[] nodes) {
        for (int i = 0; i < 5; i++) {
            for (final Boundary boundary : boundaries.get(9 - i)) {
                for (final Node node : nodes) {
                    if (boundary.contains(node.getLocation())) {
                        return boundary.getName();
                    }
                }
            }
        }

        return "[Unzugewiesen]";
    }

    private void writeCities() throws IOException {
        writeCompressedInt(cityMap.size());
        for (final Entry<String, Integer> entry : cityMap.entrySet()) {
            stream.writeUTF(entry.getKey());
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
