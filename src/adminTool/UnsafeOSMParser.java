package adminTool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import sun.misc.Unsafe;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;

public class UnsafeOSMParser {

    private final Unsafe unsafe;
    private File file;

    public UnsafeOSMParser() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void read(final File file) throws Exception {
        this.file = file;

        MemoryEstimator memoryEstimator = new MemoryEstimator();

        ReferenceCreator refCreator = new ReferenceCreator(memoryEstimator.nodeCount, memoryEstimator.wayCount);

        WayReferenceVerifier wayReferenceVerifier = new WayReferenceVerifier(refCreator.wayReferences);

        NodeReferenceVerifier nodeReferenceVerifier = new NodeReferenceVerifier(refCreator.nodeReferences);

        WayCreator wayCreator = new WayCreator(refCreator.nodeReferences, memoryEstimator.wayCount,
                memoryEstimator.wayByteCount);
        process(wayCreator, file);

        refCreator = null;

        NodeCreator nodeCreator = new NodeCreator(memoryEstimator.nodeCount);

        long nodeSpace = nodeCreator.nodeSpace;
        long waySpace = wayCreator.waySpace;

        wayCreator = null;
        memoryEstimator = null;

        process(nodeCreator, file);

    }

    private void process(final BinaryParser parser, final File file) throws IOException {
        InputStream input = new FileInputStream(file);
        new BlockInputStream(input, parser).process();
        input.close();
    }

    private class MemoryEstimator extends BinaryAdapter {

        private int nodeCount;
        private int wayCount;
        private long wayByteCount;

        public MemoryEstimator() {
            nodeCount = 207743058;
            wayCount = 32869852;
            complete();
            // try {
            // process(this, file);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
        }

        @Override
        protected void parseNodes(final List<crosby.binary.Osmformat.Node> nodes) {
            nodeCount += nodes.size();
        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            nodeCount += nodes.getIdCount();
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            wayCount += ways.size();
            for (final Way w : ways) {
                // number of nodes
                wayByteCount += w.getRefsCount() * Short.BYTES;
                // node references
                wayByteCount += w.getRefsCount() * Integer.BYTES;
            }
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {
            // relationCount += rels.size();
        }

        @Override
        public void complete() {
            // Field for way type
            wayByteCount += wayCount * Byte.BYTES;

            // Field for node references
            wayByteCount += wayCount * Integer.BYTES;

            System.out.println("Completed first scanning");
            System.out.println("Found " + nodeCount + " nodes.");
            System.out.println("Found " + wayCount + " ways.");
            // System.out.println(nodeByteCount);

            java.awt.Toolkit.getDefaultToolkit().beep();
        }

        @Override
        protected void parse(final HeaderBlock header) {

        }
    }

    private class ReferenceCreator extends BinaryAdapter {

        private long[] nodeReferences;
        private long[] wayReferences;
        private int nodeRefsCount;
        private int wayRefsCount;

        public ReferenceCreator(final int nodeCount, final int wayCount) {
            // nodeReferences = new long[nodeCount];
            // wayReferences = new long[wayCount];
            // nodeRefsCount = -1;
            // wayRefsCount = -1;
            //
            // try {
            // process(this, file);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            //
            // try {
            // DataOutputStream stream = new DataOutputStream(new
            // BufferedOutputStream(new FileOutputStream(new File(
            // "NodeReferences.data"), false)));
            // stream.writeInt(nodeReferences.length);
            // for (final long reference : nodeReferences) {
            // stream.writeLong(reference);
            // }
            // stream.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // try {
            // DataOutputStream stream = new DataOutputStream(new
            // BufferedOutputStream(new FileOutputStream(new File(
            // "WayReferences.data"), false)));
            // stream.writeInt(wayReferences.length);
            // for (final long reference : wayReferences) {
            // stream.writeLong(reference);
            // }
            // stream.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }

            try {
                DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                        "NodeReferences.data"))));
                nodeReferences = new long[stream.readInt()];
                for (int i = 0; i < nodeReferences.length; i++) {
                    nodeReferences[i] = stream.readLong();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                        "WayReferences.data"))));
                wayReferences = new long[stream.readInt()];
                for (int i = 0; i < wayReferences.length; i++) {
                    wayReferences[i] = stream.readLong();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            complete();
        }

        @Override
        protected void parseDense(DenseNodes nodes) {
            long lastId = 0;
            for (final long id : nodes.getIdList()) {
                lastId += id;
                nodeReferences[++nodeRefsCount] = lastId;
            }
        }

        @Override
        protected void parseNodes(List<Node> nodes) {
            for (final Node node : nodes) {
                nodeReferences[++nodeRefsCount] = node.getId();
            }
        }

        protected void parseWays(List<Way> ways) {
            for (final Way way : ways) {
                wayReferences[++wayRefsCount] = way.getId();
            }
        }

        @Override
        public void complete() {
            System.out.println("Node references sorted?: " + isSorted(nodeReferences));
            System.out.println("Way references sorted?: " + isSorted(wayReferences));
            java.awt.Toolkit.getDefaultToolkit().beep();
        }

        public boolean isSorted(long[] a) {
            for (int i = 0; i < a.length - 1; i++) {
                if (a[i] > a[i + 1]) {
                    return false;
                }
            }

            return true;
        }
    }

    private class WayReferenceVerifier extends BinaryAdapter {
        private long[] wayReferences;
        private boolean[] used;

        // private int count;

        public WayReferenceVerifier(final long[] wayReferences) {
            // this.wayReferences = wayReferences;
            // this.used = new boolean[wayReferences.length];
            //
            // try {
            // process(this, file);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            try {
                DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(
                        "VerifiedWayReferences.data"))));
                this.wayReferences = new long[stream.readInt()];
                for (int i = 0; i < this.wayReferences.length; i++) {
                    this.wayReferences[i] = stream.readLong();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            complete();
        }

        private void verify(final long reference) {
            int index = Arrays.binarySearch(wayReferences, reference);
            if (index >= 0) {
                used[index] = true;
            }
        }

        @Override
        protected void parseWays(final List<Way> ways) {
            String wayTag;

            int terrainType;

            boolean buildingTag;
            boolean areaTag;
            boolean tunnel;

            for (final Way w : ways) {
                buildingTag = false;
                wayTag = "";
                terrainType = -1;
                tunnel = false;
                areaTag = false;

                for (int i = 0; i < w.getKeysCount(); i++) {

                    final String key = getStringById(w.getKeys(i));
                    final String value = getStringById(w.getVals(i));

                    switch (key) {
                        case "leisure":
                            if (value.equals("track")) {
                                wayTag = "career";
                                break;
                            }
                        case "amenity":
                        case "tourism":
                        case "landuse":
                        case "natural":
                        case "man_made":
                            if (terrainType == -1) {
                                terrainType = getAreaType(value, true);
                            }
                            break;
                        case "highway":
                            if (value.equals("pedestrian")) {
                                terrainType = getAreaType("pedestrian", true);
                            }
                        case "railway":
                            wayTag = value;
                            break;
                        case "building":
                            buildingTag = true;
                            break;
                        case "waterway":
                            if (value.equals("riverbank")) {
                                terrainType = getAreaType("water", true);
                            } else {
                                wayTag = value;
                            }
                            break;
                        case "tunnel":
                            tunnel = true;
                            break;
                        case "area":
                            if (value.equals("yes")) {
                                areaTag = true;
                            }
                            break;
                        case "barrier":
                            wayTag = value;
                            break;
                    }
                }

                if (w.getRefsList().size() > 1) {

                    int type;

                    if (buildingTag) {
                        verify(w.getId());
                    } else {
                        long firstNode = w.getRefsList().get(0);
                        long lastNode = w.getRefsList().get(w.getRefsList().size() - 1);

                        if (terrainType >= 0 && firstNode == lastNode) {
                            verify(w.getId());
                        }

                        if (!areaTag) {
                            type = getStreetType(wayTag);
                            if (type >= 0) {
                                verify(w.getId());
                            } else {
                                type = getWayType(wayTag);

                                if (type >= 0) {
                                    if (!tunnel || type != 2) {
                                        verify(w.getId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void parseRelations(final List<Relation> rels) {
            for (final Relation relation : rels) {

                String relationType = "";

                for (int i = 0; i < relation.getKeysCount(); i++) {
                    final String key = getStringById(relation.getKeys(i));
                    final String value = getStringById(relation.getVals(i));
                    if (key.equals("type")) {
                        relationType = value;
                        break;
                    }
                }

                switch (relationType) {
                    case "multipolygon":
                    case "boundary":
                        long id = 0;
                        for (int j = 0; j < relation.getRolesSidCount(); j++) {
                            final String role = getStringById(relation.getRolesSid(j));
                            id += relation.getMemids(j);

                            if ((role.equals("inner") || role.equals("outer"))) {
                                verify(id);
                            }
                        }
                        break;
                }
            }

        }

        public void complete() {
            // int count = 0;
            // for (final boolean use : used) {
            // if (use) {
            // ++count;
            // }
            // }
            // long[] verifiedWayReferences = new long[count];
            // count = -1;
            // for (int i = 0; i < wayReferences.length; i++) {
            // if (used[i]) {
            // verifiedWayReferences[++count] = wayReferences[i];
            // }
            // }
            // wayReferences = verifiedWayReferences;

            // try {
            // DataOutputStream stream = new DataOutputStream(new
            // BufferedOutputStream(new FileOutputStream(new File(
            // "VerifiedWayReferences.data"), false)));
            // stream.writeInt(count);
            // for (int i = 0; i < wayReferences.length; i++) {
            // if (used[i]) {
            // stream.writeLong(wayReferences[i]);
            // }
            // }
            // stream.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }

            // System.out.println("Found " + count + " references on ways.");
            System.out.println("Found " + wayReferences.length + " references on ways.");
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private class NodeReferenceVerifier extends BinaryAdapter {
        private long[] nodeReferences;
        private boolean[] used;

        public NodeReferenceVerifier(final long[] nodeReferences) {
            this.nodeReferences = nodeReferences;
            used = new boolean[nodeReferences.length];

            try {
                process(this, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parseWays(List<Way> ways) {
            for (final Way w : ways) {
                long lastId = 0;
                for (final long id : w.getRefsList()) {
                    lastId += id;
                    int ref = Arrays.binarySearch(nodeReferences, lastId);
                    if (ref >= 0) {
                        used[ref] = true;
                    }
                }
            }
        }

        @Override
        public void complete() {
            int count = 0;
            for (final boolean use : used) {
                if (use) {
                    ++count;
                }
            }
            long[] verifiedNodeReferences = new long[count];
            count = -1;
            for (int i = 0; i < nodeReferences.length; i++) {
                if (used[i]) {
                    verifiedNodeReferences[++count] = nodeReferences[i];
                }
            }
            nodeReferences = verifiedNodeReferences;

            try {
                DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(
                        "VerifiedNodeReferences.data"), false)));
                stream.writeInt(count);
                for (int i = 0; i < nodeReferences.length; i++) {
                    if (used[i]) {
                        stream.writeLong(nodeReferences[i]);
                    }
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Found " + count + " references on nodes.");
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private class WayCreator extends BinaryAdapter {
        private long waySpace;
        private long wayHeaderAdress;
        private long wayArrayAdress;
        private long[] nodeReferences;
        private int wayCount;

        public WayCreator(final long[] nodeReferences, final int wayCount, final long wayByteCount) {
            waySpace = unsafe.allocateMemory(wayByteCount);
            System.out.println("Allocated " + wayByteCount + " bytes memory for storage of ways");
            wayArrayAdress = waySpace + wayCount * (Byte.BYTES + Integer.BYTES);
            wayHeaderAdress = waySpace;

            this.nodeReferences = nodeReferences;
        }

        @Override
        public void complete() {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }

        @Override
        protected void parseWays(List<Way> ways) {
            for (final Way w : ways) {
                unsafe.putByte(wayHeaderAdress, (byte) 0);
                unsafe.putInt(wayHeaderAdress + 1, (int) (wayArrayAdress - waySpace));
                wayHeaderAdress += (Byte.BYTES + Integer.BYTES);

                // TODO not all refs might be available...
                unsafe.putShort(wayArrayAdress, (short) w.getRefsCount());
                wayArrayAdress += Short.BYTES;

                long lastId = 0;
                for (final long id : w.getRefsList()) {
                    lastId += id;
                    int ref = Arrays.binarySearch(nodeReferences, lastId);
                    // handle ref < 0 if not found...
                    unsafe.putInt(wayArrayAdress, ref);
                    wayArrayAdress += Integer.BYTES;
                }

                if (++wayCount % 500000 == 0) {
                    System.out.println("Created " + wayCount + " ways.");
                }
            }
        }
    }

    private class NodeCreator extends BinaryAdapter {
        private long nodeSpace;
        private long nodeAdress;
        private int nodeCount;

        public NodeCreator(final int nodeCount) {
            nodeSpace = unsafe.allocateMemory(nodeCount * Integer.BYTES * 2 + Integer.BYTES);
            unsafe.putInt(nodeSpace, nodeCount);
            nodeAdress = nodeSpace;
            nodeAdress += Integer.BYTES;
        }

        @Override
        protected void parseDense(final DenseNodes nodes) {
            int lastLat = 0;
            int lastLon = 0;

            for (int i = 0; i < nodes.getIdCount(); i++) {
                lastLat += nodes.getLat(i);
                lastLon += nodes.getLon(i);

                unsafe.putInt(nodeAdress, lastLat);
                nodeAdress += Integer.BYTES;
                unsafe.putInt(nodeAdress, lastLon);
                nodeAdress += Integer.BYTES;

                if (++nodeCount % 1000000 == 0) {
                    System.out.println("Created " + nodeCount + " nodes.");
                }
            }
        }
    }

    private class BinaryAdapter extends BinaryParser {

        @Override
        public void complete() {
        }

        @Override
        protected void parseRelations(List<Relation> rels) {
        }

        @Override
        protected void parseDense(DenseNodes nodes) {
        }

        @Override
        protected void parseNodes(List<Node> nodes) {
        }

        @Override
        protected void parseWays(List<Way> ways) {
        }

        @Override
        protected void parse(HeaderBlock header) {
        }
    }

    public static void main(String[] args) {
        try {
            new UnsafeOSMParser().read(new File("Deutschland.pbf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getStreetType(final String value) {

        if (value.equals("unclassified") || value.equals("residential") || value.equals("living_street")
                || value.equals("pedestrian")) {
            return 0;
        }
        if (value.equals("service")) {
            return 1;
        }
        if (value.equals("secondary") || value.equals("secondary_link")) {
            return 2;
        }
        if (value.equals("tertiary") || value.equals("tertiary_link")) {
            return 3;
        }
        if (value.equals("road")) {
            return 4;
        }
        if (value.equals("track")) {
            return 5;
        }
        if (value.equals("footway")) {
            return 6;
        }
        if (value.equals("cycleway")) {
            return 7;
        }
        if (value.equals("bridleway")) {
            return 8;
        }
        if (value.equals("path")) {
            return 9;
        }

        return -1;
    }

    private int getWayType(final String value) {

        if (value.equals("river")) {
            return 10;
        }
        if (value.equals("stream") || value.equals("canal")) {
            return 11;
        }
        if (value.equals("rail")) {
            return 12;
        }
        if (value.equals("tram") || value.equals("light_rail")) {
            return 13;
        }
        if (value.equals("primary")) {
            return 14;
        }
        if (value.equals("motorway")) {
            return 15;
        }
        if (value.equals("trunk")) {
            return 16;
        }
        if (value.equals("primary_link")) {
            return 17;
        }
        if (value.equals("motorway_link")) {
            return 18;
        }
        if (value.equals("trunk_link")) {
            return 19;
        }
        if (value.equals("career")) {
            return 20;
        }
        if (value.equals("steps")) {
            return 21;
        }
        if (value.equals("wall") || value.equals("fence") || value.equals("retaining_wall")
                || value.equals("city_wall")) {
            return 22;
        }
        if (value.equals("hedge")) {
            return 23;
        }

        return -1;

    }

    private int getAreaType(final String value, final boolean area) {
        if (value.equals("forest") || value.equals("woodland")) {
            return 0;
        }
        if (value.equals("wood")) {
            return 1;
        }
        if (value.equals("scrub")) {
            return 2;
        }
        if (value.equals("grass") || value.equals("meadow") || value.equals("orchard") || value.equals("grassland")
                || value.equals("village_green") || value.equals("vineyard") || value.equals("allotments")
                || value.equals("recreation_ground") || value.equals("garden")) {
            return 3;
        }
        if (value.equals("greenfield") || value.equals("landfill") || value.equals("brownfield")
                || value.equals("construction") || value.equals("paddock")) {
            return 4;
        }
        if (value.equals("residential") || value.equals("railway") || value.equals("garages") || value.equals("garage")
                || value.equals("traffic_island") || value.equals("road") || value.equals("plaza")) {
            return 5;
        }
        if (value.equals("water") || value.equals("reservoir") || value.equals("basin")) {
            return 6;
        }
        if (value.equals("industrial")) {
            return 7;
        }
        if (value.equals("park")) {
            return 8;
        }
        if (value.equals("retail") || value.equals("commercial")) {
            return 9;
        }
        if (value.equals("heath") || value.equals("fell")) {
            return 10;
        }
        if (value.equals("sand") || value.equals("beach")) {
            return 11;
        }
        if (value.equals("mud") || value.equals("scree") || value.equals("bare_rock") || value.equals("pier")) {
            return 12;
        }
        if (value.equals("quarry")) {
            return 13;
        }
        if (value.equals("cemetery")) {
            return 14;
        }
        if (value.equals("parking")) {
            return 15;
        }
        if (value.equals("pedestrian")) {
            return area ? 16 : -1;
        }
        if (value.equals("farmland") || value.equals("farmyard") || value.equals("farm")) {
            return 17;
        }
        if (value.equals("playground")) {
            return 18;
        }
        if (value.equals("pitch")) {
            return 19;
        }
        if (value.equals("sports_centre") || value.equals("stadium")) {
            return 20;
        }
        if (value.equals("track")) {
            return area ? 21 : -1;
        }
        if (value.equals("golf_course") || value.equals("miniature_golf")) {
            return 22;
        }
        if (value.equals("school") || value.equals("university") || value.equals("college")
                || value.equals("kindergarten")) {
            return 23;
        }
        if (value.equals("zoo")) {
            return 24;
        }

        return -1;

    }

    private int getAmenityType(final String amenity) {

        if (amenity.equals("viewpoint")) {
            return 0;
        }
        if (amenity.equals("school")) {
            return 1;
        }
        if (amenity.equals("library")) {
            return 2;
        }
        if (amenity.equals("hospital")) {
            return 3;
        }
        if (amenity.equals("bank")) {
            return 4;
        }
        if (amenity.equals("cinema")) {
            return 5;
        }
        if (amenity.equals("museum")) {
            return 6;
        }
        if (amenity.equals("theatre")) {
            return 7;
        }
        if (amenity.equals("courthouse")) {
            return 8;
        }
        if (amenity.equals("playground")) {
            return 9;
        }
        if (amenity.equals("restaurant")) {
            return 10;
        }
        if (amenity.equals("cafe")) {
            return 11;
        }
        if (amenity.equals("bar")) {
            return 12;
        }
        if (amenity.equals("parking")) {
            return 13;
        }
        if (amenity.equals("fuel")) {
            return 14;
        }

        return -1;
    }
}
