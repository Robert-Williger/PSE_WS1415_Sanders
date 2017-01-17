package model.map;

import java.util.Iterator;

public class QuadtreeTest1 {
    private static int[] createElementData() {
        int[] ret = new int[68];

        // empty tile
        ret[0] = -1;
        ret[1] = -1;

        // root
        ret[2] = 1;
        ret[3] = -1;
        ret[4] = -1;

        // leaf [0]
        ret[5] = -1;
        ret[6] = 1;
        ret[7] = -1;
        // inner node [1]
        ret[8] = 2;
        ret[9] = -1;
        ret[10] = 1;
        ret[11] = -1;
        // inner node [2]
        ret[12] = 3;
        ret[13] = 4;
        ret[14] = -1;
        ret[15] = 1;
        ret[16] = -1;
        // leaf [3] - empty

        // leaf [10]
        ret[17] = -1;
        ret[18] = 1;
        ret[19] = -1;
        // leaf [11]
        ret[20] = -1;
        ret[21] = 2;
        ret[22] = -1;
        // leaf [12]
        ret[23] = -1;
        ret[24] = 2;
        ret[25] = -1;
        // leaf [13]
        ret[26] = -1;
        ret[27] = 2;
        ret[28] = -1;

        // inner node [20]
        ret[29] = -1;
        ret[30] = 1;
        ret[31] = 3;
        ret[32] = -1;
        // leaf [21]
        ret[33] = -1;
        ret[34] = 3;
        ret[35] = -1;
        // leaf [22]
        ret[36] = -1;
        ret[37] = 3;
        ret[38] = 4;
        ret[39] = -1;
        // inner node [23]
        ret[40] = 5;
        ret[41] = -1;
        ret[42] = 4;
        ret[43] = -1;

        // leaf [200]
        ret[44] = -1;
        ret[45] = 1;
        ret[46] = -1;
        // leaf [201] - empty
        // leaf [202]
        ret[47] = -1;
        ret[48] = 1;
        ret[49] = 3;
        ret[50] = -1;
        // leaf [203]
        ret[51] = -1;
        ret[52] = 3;
        ret[53] = -1;

        // leaf [230]
        ret[54] = -1;
        ret[55] = 5;
        ret[56] = -1;
        // leaf [231] - empty
        // inner node [232] - "empty"
        // leaf [233]
        ret[57] = 7;
        ret[58] = -1;
        ret[59] = 5;
        ret[60] = -1;

        // leaf [2320] - empty
        // leaf [2321]
        ret[61] = -1;
        ret[62] = 5;
        ret[63] = -1;
        // leaf [2322]
        ret[64] = 6;
        ret[65] = -1;
        ret[66] = 4;
        ret[67] = -1;
        // leaf [2323] - empty

        return ret;
    }

    private static int[] createTreeData() {
        int[] ret = new int[31];

        // root - inner node
        ret[0] = 2 << 1 | 1;
        ret[1] = 2;

        // leaf [0]
        ret[2] = 5 << 1 | 0;
        // inner node [1]
        ret[3] = 8 << 1 | 1;
        ret[4] = 8;
        // inner node [2]
        ret[5] = 12 << 1 | 1;
        ret[6] = 12;
        // leaf [3]
        ret[7] = 0 << 1 | 0;

        // leaf [10]
        ret[8] = 17 << 1 | 0;
        // leaf [11]
        ret[9] = 20 << 1 | 0;
        // leaf [12]
        ret[10] = 23 << 1 | 0;
        // leaf [13]
        ret[11] = 26 << 1 | 0;

        // inner node [20]
        ret[12] = 29 << 1 | 1;
        ret[13] = 18;
        // leaf [21]
        ret[14] = 33 << 1 | 0;
        // leaf [22]
        ret[15] = 36 << 1 | 0;
        // inner node [23]
        ret[16] = 40 << 1 | 1;
        ret[17] = 22;

        // leaf [200]
        ret[18] = 44 << 1 | 0;
        // leaf [201]
        ret[19] = 0 << 1 | 0;
        // leaf [202]
        ret[20] = 47 << 1 | 0;
        // leaf [203]
        ret[21] = 51 << 1 | 0;

        // leaf [230]
        ret[22] = 54 << 1 | 0;
        // leaf [231]
        ret[23] = 0 << 1 | 0;
        // inner node [232]
        ret[24] = 0 << 1 | 1;
        ret[25] = 27;
        // leaf [233]
        ret[26] = 57 << 1 | 0;

        // leaf [2320]
        ret[27] = 0 << 1 | 0;
        // leaf[2321]
        ret[28] = 61 << 1 | 0;
        // leaf[2322]
        ret[29] = 64 << 1 | 0;
        // leaf[2323]
        ret[30] = 0 << 1 | 0;

        return ret;
    }

    public static void main(final String[] args) {
        final Quadtree tree = new Quadtree(createTreeData(), createElementData(), 0);
        for (final Iterator<Long> iterator = tree.iterator(0, 0, 0); iterator.hasNext();) {
            System.out.println(iterator.next());
        }
    }
}
