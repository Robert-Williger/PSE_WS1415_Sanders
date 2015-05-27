package view;

import java.awt.event.MouseEvent;
import java.util.EventListener;

public interface IListListener extends EventListener {

    void indexChanged(int fromIndex, int toIndex);

    void indexClicked(int index, MouseEvent e);

    void indexRemoved(int index);

}