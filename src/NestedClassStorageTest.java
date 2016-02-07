public class NestedClassStorageTest {

    int a;
    int b;
    int c;
    int d;
    int e;

    public static void main(String[] args) {
        new NestedClassStorageTest();
    }

    public NestedClassStorageTest() {
        try {
            Thread.sleep(4000);
        } catch (final InterruptedException e) {

        }
        NestedClass[] classes = new NestedClass[1000];

        for (int i = 0; i < classes.length; i++) {
            classes[i] = new NestedClass();
        }

        try {
            Thread.sleep(4000);
        } catch (final InterruptedException e) {

        }
    }

    public class NestedClass {
        int[] a;
        int b;
        int c;

        public NestedClass() {
            a = new int[4];
        }
    }
}
