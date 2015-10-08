package view;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.event.DocumentListener;

public interface ISidebarView extends IView {

    void addListListener(IListListener listener);

    void addTextFieldListener(DocumentListener listener);

    void addActionListener(ActionListener listener);

    void setAddable(boolean addable);

    void setConfirmable(boolean confirmable);

    void setSearchable(boolean searchable);

    void setAddressSuggestions(List<String> list);

    void setCancelable(boolean cancelable);

    void setPointOrderChangable(boolean changeable);

    void setPOIChangeable(boolean changeable);

    void setStartable(boolean startable);

    void setResettable(boolean resettable);

    void setRouteLength(int length);

    void clearTextField();

}