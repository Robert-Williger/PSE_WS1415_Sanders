package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import model.targets.IPointList;
import model.targets.IPointListListener;
import model.targets.IPointListener;
import model.targets.IRoutePoint;
import model.targets.PointAdapter;
import model.targets.PointState;

public class PointListView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color HOVERED_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_COLOR = Color.LIGHT_GRAY;
    private static final Color NEUTRAL_COLOR = Color.WHITE;

    private final DefaultListModel<IRoutePoint> model;
    private final RoutePointRenderer renderer;
    private final ButtonPanel listButtons;
    private final JList<IRoutePoint> list;
    private int hoveredIndex;
    private boolean added;
    private boolean removed;

    public PointListView(final IPointList pointList) {
        model = new DefaultListModel<>();
        listButtons = new ButtonPanel();
        list = new JList<>(model);
        renderer = new RoutePointRenderer();

        setPointList(pointList);
        initialize();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        list.setEnabled(enabled);
        renderer.setEnabled(enabled);
        hoveredIndex = -1;
        repaint();
    }

    public void setPointList(final IPointList pointList) {
        pointList.addPointListListener(new IPointListListener() {

            @Override
            public void pointAdded(final IRoutePoint point) {
                point.addPointListener(createPointListener(point));
            }

            @Override
            public void pointRemoved(final IRoutePoint point) {
                remove(point);
            }

        });
    }

    public void addListListener(final IListListener listener) {
        listenerList.add(IListListener.class, listener);
    }

    private void initialize() {
        list.setTransferHandler(new ListTransferHandler<>(list));
        setTransferHandler(new ListTransferHandler<>(list));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDropMode(DropMode.INSERT);
        list.setCellRenderer(renderer);
        list.setDragEnabled(true);
        list.setFocusable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setBackground(NEUTRAL_COLOR);
        add(list);
        add(listButtons);

        final MouseAdapter listener = new MouseAdapter() {

            @Override
            public void mouseExited(final MouseEvent e) {
                if (isEnabled()) {
                    hoveredIndex = -1;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(final MouseEvent e) {
                if (isEnabled()) {
                    final int index = e.getY() / 20;
                    hoveredIndex = index < model.size() ? index : -1;
                    repaint();
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isEnabled()) {
                    final int index = e.getY() / 20;
                    if (index < model.size()) {
                        fireIndexClicked(index, e);
                    }

                    repaint();
                }
            }
        };

        list.addMouseMotionListener(listener);
        list.addMouseListener(listener);
        listButtons.addMouseMotionListener(listener);
        // TODO bug occurs, if point list exited by listButtons.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(final MouseEvent e) {
                if (isEnabled()) {
                    hoveredIndex = -1;
                    repaint();
                }
            }
        });

        model.addListDataListener(new ListDataListener() {
            private int fromIndex;
            private int toIndex = -1;

            @Override
            public void intervalAdded(final ListDataEvent e) {
                listButtons.add();
                if (!added) {
                    toIndex = e.getIndex0();
                } else {
                    added = false;
                }
                repaint();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                listButtons.remove();
                fromIndex = e.getIndex0();
                if (toIndex == -1) { // Item at fromIndex was removed
                    // If item was removed by IPointList, event has not to be
                    // delivered.
                    if (!removed) {
                        fireIndexRemoved(fromIndex);
                    } else {
                        removed = false;
                    }
                } else {
                    if (fromIndex < toIndex) {
                        toIndex--;
                    } else {
                        fromIndex--;
                    }

                    fireIndexChanged(fromIndex, toIndex);

                    toIndex = -1;
                }

                repaint();

            }

            @Override
            public void contentsChanged(final ListDataEvent e) {
            }
        });

        hoveredIndex = -1;
    }

    private IPointListener createPointListener(final IRoutePoint point) {
        return new PointAdapter() {
            private boolean added;

            @Override
            public void listIndexChanged() {
                if (point.getListIndex() < model.size()) {
                    model.setElementAt(point, point.getListIndex());
                }
            }

            @Override
            public void addressChanged() {
                repaint();
            }

            @Override
            public void stateChanged() {
                if (!added && point.getState() == PointState.added) {
                    add(point);
                    added = true;
                }
                repaint();
            }

            @Override
            public void targetIndexChanged() {
                repaint();
            }

        };
    }

    private void add(final IRoutePoint point) {
        added = true;
        model.addElement(point);
        listButtons.add();

    }

    private void remove(final IRoutePoint point) {
        if (model.contains(point)) {
            removed = true;
            model.removeElement(point);
            listButtons.remove();
        }
    }

    private void fireIndexChanged(final int fromIndex, final int toIndex) {
        for (final IListListener listener : listenerList.getListeners(IListListener.class)) {
            listener.indexChanged(fromIndex, toIndex);
        }
    }

    private void fireIndexClicked(final int index, final MouseEvent e) {
        for (final IListListener listener : listenerList.getListeners(IListListener.class)) {
            listener.indexClicked(index, e);
        }
    }

    private void fireIndexRemoved(final int index) {
        for (final IListListener listener : listenerList.getListeners(IListListener.class)) {
            listener.indexRemoved(index);
        }
    }

    private class ButtonPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final List<JPanel> buttonPanels;

        public ButtonPanel() {
            buttonPanels = new ArrayList<>();
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        }

        public void setHovered(final int index) {
            if (index < model.size()) {
                buttonPanels.get(index).setBackground(HOVERED_COLOR);
                buttonPanels.get(index).getComponent(0).setVisible(true);
                hoveredIndex = index;
            }
        }

        public void setSelected(final int index) {
            if (index < model.size()) {
                buttonPanels.get(index).setBackground(SELECTED_COLOR);
                buttonPanels.get(index).getComponent(0).setVisible(true);
            }
        }

        public void setHidden(final int index) {
            if (index < model.size()) {
                buttonPanels.get(index).setBackground(NEUTRAL_COLOR);
                buttonPanels.get(index).getComponent(0).setVisible(false);
            }
        }

        public void add() {
            if (buttonPanels.size() < model.getSize()) {
                final JPanel panel = new JPanel();
                panel.setPreferredSize(new Dimension(20, 20));
                panel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
                final JButton button = new JButton("x");
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setPreferredSize(new Dimension(16, 16));
                button.setOpaque(false);
                button.setFocusable(false);
                add(button);

                final int index = model.size() - 1;
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        model.remove(index);
                    }

                });

                panel.setOpaque(true);
                panel.add(button);
                panel.setBackground(NEUTRAL_COLOR);
                add(panel);

                buttonPanels.add(panel);
            }
        }

        public void remove() {
            if (buttonPanels.size() > model.getSize()) {
                remove(buttonPanels.remove(buttonPanels.size() - 1));
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(20, buttonPanels.size() * 20);
        }
    }

    private class RoutePointRenderer extends JPanel implements ListCellRenderer<IRoutePoint> {
        private static final long serialVersionUID = 1L;

        private final JLabel indexLabel;
        private final JLabel addressLabel;

        public RoutePointRenderer() {
            setPreferredSize(new Dimension(142, 20));
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
            indexLabel = new JLabel() {
                private static final long serialVersionUID = 1L;
                private final int charWidth = getFontMetrics(getFont()).stringWidth("9") + 1;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension((int) (Math.log10(model.size()) + 2) * charWidth, 16);
                }
            };
            indexLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            addressLabel = new JLabel() {
                private static final long serialVersionUID = 1L;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(138 - indexLabel.getPreferredSize().width, 16);
                }
            };

            add(indexLabel);
            add(addressLabel);
        }

        @Override
        public void setEnabled(final boolean enabled) {
            final Color color = enabled ? Color.black : Color.gray;
            indexLabel.setForeground(color);
            addressLabel.setForeground(color);
        }

        @Override
        public JPanel getListCellRendererComponent(final JList<? extends IRoutePoint> list, final IRoutePoint value,
                final int index, final boolean isSelected, final boolean cellHasFocus) {
            indexLabel.setText((value.getTargetIndex() + 1) + ". ");
            addressLabel.setText(value.getAddress());
            switch (value.getState()) {
                case added:
                    if (hoveredIndex == index) {
                        setBackground(HOVERED_COLOR);
                        listButtons.setHovered(index);
                    } else {
                        setBackground(NEUTRAL_COLOR);
                        listButtons.setHidden(index);
                    }
                    break;
                case editing:
                    setBackground(SELECTED_COLOR);
                    listButtons.setSelected(index);
                    break;
                default:
                    break;
            }

            return this;
        }
    }
}
