package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class SingleTransferable implements Transferable {

    private final DataFlavor flavor;
    private final DataFlavor[] flavors;

    public SingleTransferable(final DataFlavor flavor) {
        this.flavor = flavor;
        flavors = new DataFlavor[]{flavor};
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return flavor == this.flavor;
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
        return new Object();
    }

}
