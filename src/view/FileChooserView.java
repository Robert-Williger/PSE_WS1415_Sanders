package view;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

public class FileChooserView extends JFileChooser {
    private static final long serialVersionUID = 1L;
    private final Image icon;
    private static ImageIcon tskIcon;

    public FileChooserView(final String currentDirectoryPath, final Image icon) {
        super(currentDirectoryPath);
        this.icon = icon;
        setFileView(new FileView() {
            @Override
            public Icon getIcon(final File f) {
                if (!f.getName().endsWith(".tsk")) {
                    return FileSystemView.getFileSystemView().getSystemIcon(f);
                }
                return tskIcon;
            }
        });
    }

    public FileChooserView(final String currentDirectoryPath) {
        this(currentDirectoryPath, null);
    }

    public FileChooserView(final Image icon) {
        this(null, icon);
    }

    public FileChooserView() {
        this(null, null);
    }

    @Override
    protected JDialog createDialog(final Component parent) throws HeadlessException {
        final JDialog dialog = super.createDialog(parent);
        dialog.setIconImage(icon);
        return dialog;
    }

    static {
        tskIcon = new ImageIcon(FileChooserView.class.getResource("tsk.png"));
    }
}
