package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.IApplication;
import model.routing.IRouteManager;
import model.targets.IPointList;

public class ApplicationView extends JFrame implements IApplicationView {

    private static final long serialVersionUID = 1L;

    private final SidebarView sidebar;
    private final MapView map;
    private final ApplicationMenuBar menuBar;
    private final HelpView help;

    private final IApplication application;

    public ApplicationView(final IApplication application) {
        super("Traveling Salesman Routenplaner");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        try {
            final URL path = getClass().getResource("icon.png");
            if (path != null) {
                setIconImage(ImageIO.read(path));
            }
        } catch (final IOException e) {

        }

        final IRouteManager manager = application.getRouteManager();
        final IPointList list = manager.getPointList();

        initialize(application);

        sidebar = new SidebarView(manager);
        map = new MapView(application.getImageLoader(), list, application.getMap());
        menuBar = new ApplicationMenuBar();
        help = new HelpView();

        setContentPane(new ContentPane());
        getContentPane().add(map, BorderLayout.CENTER);
        getContentPane().add(sidebar, BorderLayout.WEST);

        setJMenuBar(menuBar);
        setTransferHandler(new DeleteTransferHandler());
        help.setVisible(false);

        this.application = application;

        manager.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                menuBar.setCalculating(manager.isCalculating());
            }
        });

        application.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                final IApplication application = ApplicationView.this.application;
                final IRouteManager manager = application.getRouteManager();
                sidebar.setModel(manager);
                map.setModels(application.getImageLoader(), manager.getPointList(), application.getMap());
                manager.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(final ChangeEvent e) {
                        menuBar.setCalculating(manager.isCalculating());
                    }
                });
                repaint();
            }

        });
    }

    private void initialize(final IApplication application) {
        setSize(800, 640);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setVisible(true);
    }

    @Override
    public void addMenuBarListener(final ActionListener listener) {
        menuBar.addActionListener(listener);
    }

    @Override
    public ISidebarView getSidebar() {
        return sidebar;
    }

    @Override
    public IMapView getMap() {
        return map;
    }

    @Override
    public void setHelpVisible(final boolean visible) {
        if (visible) {
            help.setLocationRelativeTo(null);
        }
        help.setVisible(visible);
    }

    private class ContentPane extends JPanel {
        private static final long serialVersionUID = 1L;

        public ContentPane() {
            super(new BorderLayout());
        }

        @Override
        public void paintComponent(final Graphics g) {
            final float[] FRACTIONS = {0.0f, 1.0f};
            final Color[] FILL_COLORS = {Color.white, new Color(230, 230, 230)};
            final Paint paint = new LinearGradientPaint(0, 0, getWidth(), (int) Math.sqrt(getHeight() * getHeight()
                    + getWidth() * getWidth()) / 2, FRACTIONS, FILL_COLORS, MultipleGradientPaint.CycleMethod.REFLECT);
            ((Graphics2D) g).setPaint(paint);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class ApplicationMenuBar extends JMenuBar {
        private static final long serialVersionUID = 1L;

        final JMenuItem importItem;
        final JMenuItem exportItem;
        final JMenuItem exitItem;
        final JMenuItem helpItem;

        public ApplicationMenuBar() {
            super();

            final JMenu file = new JMenu("Datei");
            file.setMnemonic('D');
            importItem = new JMenuItem("Kartenpfad ändern", new ImageIcon(
                    ApplicationView.class.getResource("import.png")));
            importItem.setActionCommand("import");

            exportItem = new JMenuItem("Karte exportieren", new ImageIcon(
                    ApplicationView.class.getResource("export.png")));
            exportItem.setActionCommand("export");

            exitItem = new JMenuItem("Beenden", new ImageIcon(ApplicationView.class.getResource("exit.png")));
            exitItem.setActionCommand("exit");

            file.add(importItem);
            file.add(exportItem);
            file.addSeparator();
            file.add(exitItem);

            final JMenu help = new JMenu("Hilfe");
            help.setMnemonic('H');
            helpItem = new JMenuItem("Hilfe", new ImageIcon(ApplicationView.class.getResource("help.png")));
            helpItem.setActionCommand("help");

            help.add(helpItem);

            add(file);
            add(help);
        }

        public void addActionListener(final ActionListener l) {
            importItem.addActionListener(l);
            exportItem.addActionListener(l);
            exitItem.addActionListener(l);
            helpItem.addActionListener(l);
        }

        public void setCalculating(final boolean calculating) {
            importItem.setEnabled(!calculating);
            exportItem.setEnabled(!calculating);
        }
    }

}