package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import model.targets.IRoutePoint;

public class ListTransferHandler<T> extends TransferHandler {
    private static final long serialVersionUID = 1L;

    private final JList<T> list;
    private final DefaultListModel<T> listModel;

    private final DataFlavor flavor;
    private T selectedValue;

    public ListTransferHandler(final JList<T> list) {
        if (!(list.getModel() instanceof DefaultListModel)) {
            throw new IllegalArgumentException(
                    "Invalid listmodel. It has to be subclass of DefaultListModel for being able to be modified.");
        }

        this.list = list;
        this.listModel = (DefaultListModel<T>) list.getModel();
        this.flavor = new DataFlavor(IRoutePoint.class, "Test");
    }

    @Override
    public boolean canImport(final TransferHandler.TransferSupport info) {
        return info.getTransferable().isDataFlavorSupported(flavor);
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        selectedValue = list.getSelectedValue();

        return new SingleTransferable(flavor);
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport info) {
        if (selectedValue == null || !info.isDrop()) {
            return false;
        }

        final JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();

        if (dl.isInsert()) {
            listModel.add(dl.getIndex(), selectedValue);
        }

        return true;
    }

    @Override
    protected void exportDone(final JComponent c, final Transferable data, final int action) {
        if (action == TransferHandler.MOVE) {
            listModel.remove(list.getSelectedIndex());
        }
    }
}