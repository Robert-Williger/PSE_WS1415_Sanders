package view;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class DeleteTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean canImport(final TransferHandler.TransferSupport info) {
        return true;
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        return null;
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.NONE;
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport info) {
        return true;
    }
}
