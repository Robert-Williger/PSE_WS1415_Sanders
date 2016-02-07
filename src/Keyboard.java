public class Keyboard {

    private char[][] neighbours;

    public Keyboard() {
        System.out.println((int) "€".charAt(0));
        final char[][] keyboard = new char[][]{{//
                'q', 'w', 'e', 'r', 't', 'z', 'u', 'i', 'o', 'p'}, // first row
                {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'}, // second row
                {'y', 'x', 'c', 'v', 'b', 'n', 'm'}};

    }

    public static void main(String[] args) {
        new Keyboard();
    }
}
