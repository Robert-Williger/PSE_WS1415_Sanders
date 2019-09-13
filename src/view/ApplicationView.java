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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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
import model.renderEngine.IImageAccessor;
import model.renderEngine.IImageLoader;
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

        initialize();

        sidebar = new SidebarView(manager);
        map = new MapView(application.getImageLoader(), list, application.getMap());
        menuBar = new ApplicationMenuBar(application);
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
                setTitle("Traveling Salesman Routenplaner - " + application.getName());
                sidebar.setModel(manager);
                map.setModels(application.getImageLoader(), manager.getPointList(), application.getMap());
                manager.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(final ChangeEvent e) {
                        menuBar.setCalculating(manager.isCalculating());
                    }
                });
                menuBar.setModel(application);
                repaint();
            }

        });
    }

    private void initialize() {
        setSize(1200, 640);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
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

    private class ContentPane extends JPanel {
        private static final long serialVersionUID = 1L;

        public ContentPane() {
            super(new BorderLayout());
        }

        @Override
        public void paintComponent(final Graphics g) {
            final float[] FRACTIONS = { 0.0f, 1.0f };
            final Color[] FILL_COLORS = { Color.white, new Color(230, 230, 230) };
            final Paint paint = new LinearGradientPaint(0, 0, getWidth(),
                    (int) Math.sqrt(getHeight() * getHeight() + getWidth() * getWidth()) / 2, FRACTIONS, FILL_COLORS,
                    MultipleGradientPaint.CycleMethod.REFLECT);
            ((Graphics2D) g).setPaint(paint);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class ApplicationMenuBar extends JMenuBar {
        private static final long serialVersionUID = 1L;

        final JMenu layersItem;
        final List<JMenuItem> staticItems;
        final List<JMenuItem> dynamicItems;
        final List<ActionListener> listeners;

        public ApplicationMenuBar(final IApplication application) {
            super();

            staticItems = new ArrayList<>();
            dynamicItems = new ArrayList<>();
            listeners = new ArrayList<>();

            final JMenu file = new JMenu("Datei");
            file.setMnemonic('D');
            final JMenuItem importItem = new JMenuItem("Kartenpfad Ändern",
                    new ImageIcon(ApplicationView.class.getResource("import.png")));
            importItem.setActionCommand("import");

            final JMenuItem exportItem = new JMenuItem("Karte exportieren",
                    new ImageIcon(ApplicationView.class.getResource("export.png")));
            exportItem.setActionCommand("export");

            final JMenuItem exitItem = new JMenuItem("Beenden",
                    new ImageIcon(ApplicationView.class.getResource("exit.png")));
            exitItem.setActionCommand("exit");

            file.add(importItem);
            file.add(exportItem);
            file.addSeparator();
            file.add(exitItem);

            layersItem = new JMenu("Ebenen");
            layersItem.setMnemonic('E');

            final JMenu help = new JMenu("Hilfe");
            help.setMnemonic('H');
            final JMenuItem helpItem = new JMenuItem("Hilfe",
                    new ImageIcon(ApplicationView.class.getResource("help.png")));
            helpItem.setActionCommand("help");

            help.add(helpItem);

            add(file);
            add(layersItem);
            add(help);

            staticItems.add(importItem);
            staticItems.add(exportItem);
            staticItems.add(exitItem);
            staticItems.add(helpItem);

            setModel(application);
        }

        public void addActionListener(final ActionListener l) {
            for (final JMenuItem item : staticItems)
                item.addActionListener(l);
            for (final JMenuItem item : dynamicItems)
                item.addActionListener(l);
            layersItem.addActionListener(l);

            listeners.add(l);
        }

        public void setCalculating(final boolean calculating) {
            // importItem.setEnabled(!calculating);
            // exportItem.setEnabled(!calculating);
        }

        public void setModel(final IApplication application) {
            final IImageLoader imageLoader = application.getImageLoader();
            layersItem.removeAll();
            for (final IImageAccessor accessor : imageLoader.getImageAccessors()) {
                final JCheckBoxMenuItem item = new JCheckBoxMenuItem(accessor.getName());
                accessor.addChangeListener(e -> item.setSelected(accessor.isVisible()));
                item.setActionCommand(item.getText());
                item.setSelected(accessor.isVisible());
                layersItem.add(item);
                dynamicItems.add(item);
                for (final ActionListener listener : listeners)
                    item.addActionListener(listener);
            }
        }
    }

    @Override
    public IHelpView getHelp() {
        return help;
    }

}