import java.util.function.Consumer;

/* w  ww.  ja va2s  . c o m*/
public class ConsumerTest {
    public static void main(String[] args) {
    Consumer<String> c = x -> System.out.println(x.toLowerCase());
    c.andThen(c).accept("Java2s.com");
  }
}