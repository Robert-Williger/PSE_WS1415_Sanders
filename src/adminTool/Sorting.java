package adminTool;

import adminTool.elements.Typeable;

public class Sorting<T extends Typeable> {
    public final T[] elements;
    public final int[] distribution;

    public Sorting(final T[] elements, final int[] distribution) {
        this.elements = elements;
        this.distribution = distribution;
    }
}
