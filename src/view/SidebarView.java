package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

import model.IProgressListener;
import model.routing.IRouteManager;
import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IPointListener;
import model.targets.IRoutePoint;
import model.targets.PointState;

public class SidebarView extends JPanel implements ISidebarView {

    private static final long serialVersionUID = 1L;

    private final DefaultTextedTextField textField;
    private final MultiFunctionButton multiFuncButton;
    private final JButton upButton;
    private final JButton downButton;
    private final JButton cancelButton;
    private final JButton startButton;
    private final JButton resetButton;
    private final JButton cancelCalcButton;
    private final JCheckBox poiBox;
    private final JCheckBox labelBox;
    private final JComboBox<String> routeSolverBox;
    private final SuggestionView suggestionView;
    private final PointListView listView;
    private final JProgressBar progressBar;
    private final RouteLabel routeLabel;
    private final JPanel content;
    private final JPanel gap;

    private final IPointListListener pointListener;
    private final IProgressListener progressListener;

    public SidebarView(final IRouteManager manager) {
        super(new BorderLayout());

        textField = new DefaultTextedTextField();
        multiFuncButton = new MultiFunctionButton();
        upButton = new JButton("\u2191");
        downButton = new JButton("\u2193");
        cancelButton = new JButton("Abbrechen");
        startButton = new JButton("Start");
        resetButton = new JButton("Reset");
        cancelCalcButton = new JButton("Routenberechnung abbrechen");
        poiBox = new CustomizedCheckBox("Sonderziele einblenden", "poi");
        labelBox = new CustomizedCheckBox("Karte beschriften", "label");
        routeSolverBox = new CustomizedComboBox(manager.getRouteSolvers());
        suggestionView = new SuggestionView();
        listView = new PointListView(manager.getPointList());
        routeLabel = new RouteLabel();
        progressBar = new JProgressBar(0, 100);
        content = createContent();
        gap = createGap(6, 0);

        pointListener = new IPointListListener() {

            @Override
            public void pointAdded(final IRoutePoint point) {
                point.addPointListener(new PointListener(point));
            }

            @Override
            public void pointRemoved(final IRoutePoint point) {
            }

        };

        progressListener = new ProgressListener();

        setModel(manager);
        initialize();
    }

    public void setModel(final IRouteManager manager) {
        final IPointList list = manager.getPointList();
        list.addPointListListener(pointListener);
        manager.addProgressListener(progressListener);
        listView.setPointList(list);

        manager.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setCalculating(manager.isCalculating());
            }
        });
        resetView();
    }

    private void initialize() {
        add(gap, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(createHider(), BorderLayout.EAST);

        setBorder(new LineBorder(Color.black, 1));
        setFocusable(true);
        setOpaque(false);
    }

    private void resetView() {
        multiFuncButton.setEnabled(false);
        textField.clear();
        setRouteLength(0);
        progressBar.setVisible(false);
        setPointOrderChangable(false);
        setResettable(false);
        setStartable(false);
        setCancelable(false);
        routeSolverBox.getModel().setSelectedItem(routeSolverBox.getItemAt(0));
        cancelCalcButton.setEnabled(false);
        revalidate();
    }

    private JPanel createGap(final int width, final int height) {
        final JPanel ret = new JPanel();

        ret.setPreferredSize(new Dimension(width, height));
        ret.setOpaque(false);

        return ret;
    }

    private JPanel createHider() {
        final JPanel ret = createGap(6, 0);

        ret.add(new HideButton());

        return ret;
    }

    private JPanel createContent() {
        final JPanel ret = new JPanel();

        ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

        ret.add(createGap(0, 10));
        ret.add(createTop());
        ret.add(createGap(0, 0));
        ret.add(createMid());
        ret.add(createGap(0, 0));
        ret.add(createBot());
        ret.add(createGap(0, 10));

        ret.setOpaque(false);
        ret.setPreferredSize(new Dimension(210, 0));

        return ret;
    }

    private JPanel createTop() {
        final JPanel ret = new JPanel(new FlowLayout());

        textField.setDefaultText("Ziel auswählen");
        textField.setPreferredSize(new Dimension(140, 21));
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    suggestionView.setVisible(false);
                    multiFuncButton.doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    suggestionView.setVisible(false);
                    cancelButton.doClick();
                    textField.clear();
                    requestFocus();
                }
            }
        });

        // TODO improve this?
        textField.addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {
                if (getRootPane() != null && e.getOppositeComponent() == getRootPane()) {
                    textField.requestFocus();
                }
            }
        });

        ret.add(textField);

        ret.add(multiFuncButton);

        upButton.setPreferredSize(new Dimension(23, 23));
        upButton.setMargin(new Insets(2, 2, 2, 2));
        upButton.setActionCommand("move up");
        upButton.setOpaque(false);
        upButton.setFocusable(false);
        ret.add(upButton);

        downButton.setPreferredSize(new Dimension(23, 23));
        downButton.setMargin(new Insets(2, 2, 2, 2));
        downButton.setActionCommand("move down");
        downButton.setOpaque(false);
        downButton.setFocusable(false);
        ret.add(downButton);

        ret.add(cancelButton);
        cancelButton.setOpaque(false);
        cancelButton.setActionCommand("cancel");

        ret.setOpaque(false);
        ret.setPreferredSize(new Dimension(180, 62));
        ret.setMaximumSize(new Dimension(180, 62));

        return ret;
    }

    private JPanel createMid() {
        final JPanel ret = new JPanel();

        ret.add(new PointListScroller(listView));

        progressBar.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(170, 13));
        progressBar.setVisible(false);
        ret.add(progressBar);

        ret.setOpaque(false);
        ret.add(routeLabel);
        setRouteLength(0);

        return ret;
    }

    private JPanel createBot() {
        final JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));

        routeSolverBox.setBorder(null);
        routeSolverBox.setFocusable(false);
        routeSolverBox.setOpaque(false);
        ret.add(routeSolverBox);

        poiBox.setMargin(new Insets(0, 0, 0, 0));
        poiBox.setFocusable(false);
        poiBox.setSelected(true);
        poiBox.setOpaque(false);
        ret.add(poiBox);

        labelBox.setMargin(new Insets(0, 0, 0, 0));
        labelBox.setFocusable(false);
        labelBox.setSelected(true);
        labelBox.setOpaque(false);
        ret.add(labelBox);

        startButton.setPreferredSize(new Dimension(85, 23));
        startButton.setActionCommand("start calculation");
        startButton.setEnabled(false);
        startButton.setOpaque(false);
        startButton.setFocusable(false);
        ret.add(startButton);

        resetButton.setPreferredSize(new Dimension(85, 23));
        resetButton.setActionCommand("reset");
        resetButton.setEnabled(false);
        resetButton.setOpaque(false);
        resetButton.setFocusable(false);
        ret.add(resetButton);

        cancelCalcButton.setMargin(new Insets(2, 0, 2, 0));
        cancelCalcButton.setPreferredSize(new Dimension(175, 23));
        cancelCalcButton.setActionCommand("cancel calculation");
        cancelCalcButton.setEnabled(false);
        cancelCalcButton.setOpaque(false);
        ret.add(cancelCalcButton);

        ret.setOpaque(false);
        ret.setPreferredSize(new Dimension(180, 130));
        ret.setMaximumSize(new Dimension(180, 130));
        ret.setAlignmentY(CENTER_ALIGNMENT);

        return ret;
    }

    @Override
    public void addListListener(final IListListener listener) {
        listView.addListListener(listener);
    }

    @Override
    public void addTextFieldListener(final DocumentListener listener) {
        textField.getDocument().addDocumentListener(listener);
    }

    @Override
    public void addActionListener(final ActionListener listener) {
        multiFuncButton.addActionListener(listener);
        upButton.addActionListener(listener);
        downButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        startButton.addActionListener(listener);
        resetButton.addActionListener(listener);
        cancelCalcButton.addActionListener(listener);
        poiBox.addActionListener(listener);
        labelBox.addActionListener(listener);
        routeSolverBox.addActionListener(listener);
    }

    @Override
    public void setAddable(final boolean addable) {
        multiFuncButton.setAddable(addable);
    }

    @Override
    public void setConfirmable(final boolean confirmable) {
        multiFuncButton.setConfirmable(confirmable);
    }

    @Override
    public void setSearchable(final boolean searchable) {
        multiFuncButton.setSearchable(searchable);
    }

    @Override
    public void setAddressSuggestions(final List<String> list) {
        suggestionView.setSuggestions(list);
    }

    @Override
    public void setCancelable(final boolean cancelable) {
        cancelButton.setEnabled(cancelable);
    }

    @Override
    public void setPointOrderChangable(final boolean changeable) {
        upButton.setEnabled(changeable);
        downButton.setEnabled(changeable);
    }

    @Override
    public void setStartable(final boolean startable) {
        startButton.setEnabled(startable);
    }

    @Override
    public void setResettable(final boolean resettable) {
        resetButton.setEnabled(resettable);
    }

    @Override
    public void setRouteLength(final int length) {
        routeLabel.setLength(length);
        routeLabel.setVisible(true);
    }

    @Override
    public void clearTextField() {
        textField.clear();
        requestFocus();
    }

    public void setCalculating(final boolean calculating) {
        routeSolverBox.setEnabled(!calculating);
        cancelCalcButton.setEnabled(calculating);
        progressBar.setVisible(calculating);
        listView.setEnabled(!calculating);
        textField.setEnabled(!calculating);
        routeLabel.setVisible(!calculating);
    }

    private class PointListScroller extends JScrollPane {
        private static final long serialVersionUID = 1L;

        private final PointListView listView;

        public PointListScroller(final PointListView listView) {
            super(listView);
            this.listView = listView;
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
            setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        @Override
        public void setEnabled(final boolean enabled) {
            listView.setEnabled(enabled);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SidebarView.this.getWidth() - 46, (SidebarView.this.getHeight() - 320));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(SidebarView.this.getWidth() - 46, (SidebarView.this.getHeight() - 320));
        }
    }

    private class MultiFunctionButton extends JButton {
        private static final long serialVersionUID = 1L;

        public MultiFunctionButton() {
            setPreferredSize(new Dimension(23, 23));
            setOpaque(false);
            setFocusable(false);
            setMargin(new Insets(2, 2, 2, 2));
        }

        public void setAddable(final boolean addable) {
            setActionCommand("add point");
            setText("+");
            setEnabled(addable);
        }

        public void setSearchable(final boolean searchable) {
            setActionCommand("search point");
            setText(searchable ? "?" : "+");
            setEnabled(searchable);
        }

        public void setConfirmable(final boolean confirmable) {
            setActionCommand("confirm");
            setText(confirmable ? "\u2713" : "+");
            setEnabled(confirmable);
        }
    }

    private class SuggestionView extends JPopupMenu {
        private static final long serialVersionUID = 1L;
        private static final int MAX_SUGGESTIONS = 5;

        private final JMenuItem[] menuItems;
        private final JMenuItem defaultItem;

        public SuggestionView() {
            setBackground(Color.white);
            setOpaque(true);
            menuItems = new JMenuItem[MAX_SUGGESTIONS];

            for (int i = 0; i < MAX_SUGGESTIONS; i++) {
                menuItems[i] = installNewItem();
            }
            defaultItem = installNewItem();
            defaultItem.setText("Kein Ziel gefunden");
            defaultItem.setFont(defaultItem.getFont().deriveFont(Font.ITALIC));
            add(defaultItem);
        }

        private JMenuItem installNewItem() {
            final JMenuItem ret = new JMenuItem();

            ret.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(final ChangeEvent e) {
                    if (ret.isArmed()) {
                        textField.setText(ret.getText());
                    }
                }
            });

            ret.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    textField.setText(ret.getText());
                    multiFuncButton.doClick();
                }
            });

            ret.setIconTextGap(0);
            // TODO improve this
            ret.setMargin(new Insets(0, -25, 0, 0));
            ret.setBackground(Color.white);
            ret.setOpaque(true);
            ret.setHorizontalTextPosition(JMenuItem.LEFT);
            ret.setHorizontalAlignment(SwingConstants.LEFT);
            ret.setFont(new Font("Tahoma", Font.PLAIN, 11));
            add(ret);

            return ret;
        }

        @Override
        public void paintComponent(final Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        public void setSuggestions(final List<String> addresses) {
            final int suggestions;

            if (addresses.size() != 0) {
                suggestions = Math.min(addresses.size(), MAX_SUGGESTIONS);
                for (int i = 0; i < suggestions; i++) {
                    menuItems[i].setText(addresses.get(i));
                    menuItems[i].setVisible(true);
                }
                for (int i = suggestions; i < MAX_SUGGESTIONS; i++) {
                    menuItems[i].setVisible(false);
                }
                defaultItem.setVisible(false);
            } else {
                suggestions = 1;
                defaultItem.setVisible(true);
                for (final JMenuItem item : menuItems) {
                    item.setVisible(false);
                }
            }

            setPreferredSize(new Dimension(textField.getWidth(), suggestions * 22));

            if (!isVisible()) {
                show(textField, 0, textField.getHeight() - 1);
            }
        }
    }

    private class HideButton extends JButton {
        private static final long serialVersionUID = 1L;

        private boolean hidden;

        public HideButton() {
            super("<");
            setPreferredSize(new Dimension(20, 20));
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            setBorder(null);
            setFocusable(false);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (hidden) {
                        content.setPreferredSize(new Dimension(210, 0));
                        gap.setPreferredSize(new Dimension(6, 0));
                        setText("<");
                    } else {
                        content.setPreferredSize(new Dimension(0, 0));
                        gap.setPreferredSize(new Dimension(0, 0));
                        setText(">");
                    }
                    hidden = !hidden;

                    revalidate();
                }

            });
        }
    }

    private class RouteLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private static final int MAX_METERS = 1_000;

        public void setLength(final int length) {

            if (length == 0) {
                setText(" Routenlänge: -");
            } else if (length > MAX_METERS) {
                setText(" Routenlänge: " + String.format("%.2f", length / 1000.0) + " km");
            } else {
                setText(" Routenlänge: " + length + " m");
            }
        }
    }

    private class CustomizedCheckBox extends JCheckBox {
        private static final long serialVersionUID = 1L;

        private final String command;

        public CustomizedCheckBox(final String text, final String command) {
            super(text);
            this.command = command;
        }

        @Override
        public String getActionCommand() {
            return command + " " + (isSelected() ? "enabled" : "disabled");
        }
    }

    private class CustomizedComboBox extends JComboBox<String> {
        private static final long serialVersionUID = 1L;

        public CustomizedComboBox(final String[] names) {
            super(names);
        }

        @Override
        public String getActionCommand() {
            return getModel().getSelectedItem().toString();
        }
    }

    private class PointListener implements IPointListener {
        private final IRoutePoint point;

        public PointListener(final IRoutePoint point) {
            this.point = point;
        }

        @Override
        public void indexChanged() {
            repaint();
        }

        @Override
        public void addressChanged() {
            textField.setText(point.getAddress());
            textField.requestFocus();
            repaint();
        }

        @Override
        public void locationChanged() {
            repaint();
        }

        @Override
        public void stateChanged() {
            final PointState state = point.getState();
            if (state == PointState.editing || state == PointState.unadded) {
                textField.setText(point.getAddress());
                textField.requestFocus();
            }
        }
    }

    private class ProgressListener implements IProgressListener {
        @Override
        public void progressDone(final int progress) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    progressBar.setValue(progress);
                }

            });
        }

        @Override
        public void errorOccured(final String message) {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showConfirmDialog(null, message, "Fehler bei Routenberechnung",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }

            });
        }

        @Override
        public void stepCommenced(final String step) {

        }
    }
}