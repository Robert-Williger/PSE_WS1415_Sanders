package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import model.targets.IRoutePoint;

public abstract class ListTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 1L;

    private final JList<?> list;
    private final DataFlavor flavor;

    private int selectedIndex;
    private int targetIndex;

    public ListTransferHandler(final JList<?> list) {
        this.list = list;
        this.flavor = new DataFlavor(IRoutePoint.class, "Test");
        targetIndex = -1;
        selectedIndex = -1;
    }

    @Override
    public boolean canImport(final TransferHandler.TransferSupport info) {
        return info.getTransferable().isDataFlavorSupported(flavor);
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        selectedIndex = list.getSelectedIndex();

        return new SingleTransferable(flavor);
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport info) {
        if (selectedIndex == -1 || !info.isDrop()) {
            return false;
        }

        final JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();

        if (dl.isInsert()) {
            targetIndex = dl.getIndex();
        }

        return true;
    }

    @Override
    protected void exportDone(final JComponent c, final Transferable data, final int action) {
        if (action == TransferHandler.MOVE) {
            final int fromIndex = selectedIndex;
            if (targetIndex == -1) {
                handleRemove(fromIndex);
            } else {
                final int toIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
                handleChange(fromIndex, toIndex);
            }
        }

        targetIndex = -1;
    }

    protected abstract void handleRemove(final int index);

    protected abstract void handleChange(final int fromIndex, final int toIndex);
}