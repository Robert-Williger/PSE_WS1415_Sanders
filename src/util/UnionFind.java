package util;

public class UnionFind {

    private final int[] parent;

    public UnionFind(final int nodes) {
        this.parent = new int[nodes];
        java.util.Arrays.fill(parent, nodes);
    }

    public int find(final int node) {
        if (parent[node] >= parent.length)
            return node;

        final int p = find(parent[node]);
        parent[node] = p;
        return p;
    }

    public boolean union(final int i, final int j) {
        final int pi = find(i);
        final int pj = find(j);

        if (pi == pj)
            return false;
        link(pi, pj);
        return true;
    }

    public void link(final int i, final int j) {
        if (parent[i] < parent[j])
            parent[i] = j;
        else {
            if (parent[i] == parent[j])
                ++parent[i];
            parent[j] = i;
        }
    }
}
