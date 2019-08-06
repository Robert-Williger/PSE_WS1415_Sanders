package adminTool.labeling.roadGraph;

import static adminTool.util.IntersectionUtil.EPSILON;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adminTool.elements.MultiElement;
import adminTool.util.IntersectionUtil;
import util.IntList;

public class CutPerformer {
    public List<IntList> performCuts(final MultiElement path, final List<Cut> cuts) {
        Collections.sort(cuts);
        return performSortedCuts(path, cuts);
    }

    public ArrayList<IntList> performSortedCuts(final MultiElement path, final List<Cut> cuts) {
        final ArrayList<IntList> resultList = new ArrayList<>(cuts.size() + 1);
        if (!cuts.isEmpty()) {
            Cut last = new Cut(path.getNode(0), 0, 0);
            for (int i = 0; i < cuts.size(); ++i) {
                final Cut current = cuts.get(i);
                resultList.add(performCut(path, last, current));
                last = current;
            }

            resultList.add(performCut(path, last, new Cut(path.getNode(path.size() - 1), path.size() - 2, 1)));
        } else
            resultList.add(path.toList());

        return resultList;
    }

    private static IntList performCut(final MultiElement path, final Cut previous, final Cut current) {
        final int prevSegment = previous.getSegment() + (int) Math.min(1, Math.max(0, previous.getOffset() + EPSILON));
        final int size = current.getSegment() - prevSegment + 2;
        IntList ret = new IntList(size);
        ret.add(previous.getPoint());
        for (int i = 1; i < size - 1; ++i) {
            ret.add(path.getNode(prevSegment + i));
        }
        ret.add(current.getPoint());
        return ret;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp = Double.doubleToLongBits(offset);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + segment;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return equals((Cut) obj);
        }

        public boolean equals(final Cut other) {
            return equals(other.getSegment(), other.getOffset());
        }

        public boolean equals(final int segment, final double offset) {
            return Math.abs(this.offset - offset + this.segment - segment) <= IntersectionUtil.EPSILON;
        }

        @Override
        public int compareTo(final Cut o) {
            return compareTo(o.segment, o.offset);
        }

        public int compareTo(final int segment, final double offset) {
            int ret = Integer.compare(this.segment, segment);
            if (ret == 0) {
                ret = Double.compare(this.offset, offset);
            }
            return ret;
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
