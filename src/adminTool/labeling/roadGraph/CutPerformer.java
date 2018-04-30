package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adminTool.elements.MultiElement;

public class CutPerformer {

    public List<MultiElement> performCuts(final MultiElement path, final List<Cut> cuts) {
        final List<MultiElement> resultList = new ArrayList<MultiElement>(cuts.size() + 1);
        if (!cuts.isEmpty()) {
            Collections.sort(cuts);

            Cut last = new Cut(path.getNode(0), 0, 0);
            for (int i = 0; i < cuts.size(); ++i) {
                final Cut current = cuts.get(i);
                resultList.add(performCut(path, last, current));
                last = current;
            }

            resultList.add(performCut(path, last, new Cut(path.getNode(path.size() - 1), path.size() - 2, 1)));
        } else {
            resultList.add(path);
        }
        return resultList;
    }

    private static MultiElement performCut(final MultiElement path, final Cut previous, final Cut current) {
        int[] indices = new int[current.getSegment() - previous.getSegment() + 2];
        indices[0] = previous.getPoint();
        for (int i = 1; i < indices.length - 1; ++i) {
            indices[i] = path.getNode(previous.getSegment() + i);
        }
        indices[indices.length - 1] = current.getPoint();
        return new MultiElement(indices, path.getType());
    }

    public static class Cut implements Comparable<Cut> {
        private double offset;
        private int segment;
        private int point;

        public Cut(final int point, final int segment, final double offset) {
            this.point = point;
            this.offset = offset;
            this.segment = segment;
        }

        @Override
        public int compareTo(final Cut o) {
            return compareTo(o.segment, o.offset);
        }

        public int compareTo(final int segment, final double offset) {
            return Double.compare(this.segment + this.offset, segment + offset);
        }

        @Override
        public String toString() {
            return "Cut[offset = " + offset + ", segment = " + segment + ", point = " + point + "]";
        }

        public double getOffset() {
            return offset;
        }

        public int getSegment() {
            return segment;
        }

        public int getPoint() {
            return point;
        }

        public void setOffset(final double offset) {
            this.offset = offset;
        }

        public void setSegment(final int segment) {
            this.segment = segment;
        }

        public void setPoint(final int point) {
            this.point = point;
        }
    }
}
