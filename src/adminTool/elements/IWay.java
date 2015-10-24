package adminTool.elements;

import java.util.Iterator;

public interface IWay {

    int getType();

    String getName();

    Iterator<Node> iterator();

    Iterator<Node> descendingIterator();

}
