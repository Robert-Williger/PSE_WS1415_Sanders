package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

public class PointListView extends JList<IRoutePoint> {
    private static final long serialVersionUID = 1L;

    private static final Color HOVERED_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_COLOR = Color.LIGHT_GRAY;
    private static final Color NEUTRAL_COLOR = Color.WHITE;

    private static int CELL_SIZE = 20;

    private final DefaultListModel<IRoutePoint> model;
    private final RoutePointRenderer renderer;
    private int hoveredIndex;
    private boolean added;
    private boolean removed;

    public PointListView(final IPointList pointList) {
        model = new DefaultListModel<IRoutePoint>();
        setModel(model);
        renderer = new RoutePointRenderer();

        setPointList(pointList);
        initialize();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
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
        setTransferHandler(new ListTransferHandler<IRoutePoint>(this));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDropMode(DropMode.INSERT);
        setCellRenderer(renderer);
        setDragEnabled(true);
        setFocusable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setBackground(NEUTRAL_COLOR);

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
                    final int index = e.getY() / CELL_SIZE;
                    hoveredIndex = index < model.size() ? index : -1;
                    repaint();
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isEnabled()) {
                    final int index = e.getY() / CELL_SIZE;
                    if (index < model.size()) {
                        if (renderer.button.contains(e.getX() - renderer.button.getX(), e.getY() - hoveredIndex
                                * CELL_SIZE)) {
                            model.remove(index);
                        } else {
                            fireIndexClicked(index, e);
                        }
                    }

                    repaint();
                }
            }
        };

        addMouseMotionListener(listener);
        addMouseListener(listener);

        model.addListDataListener(new ListDataListener() {
            private int fromIndex;
            private int toIndex = -1;

            @Override
            public void intervalAdded(final ListDataEvent e) {
                if (!added) {
                    toIndex = e.getIndex0();
                } else {
                    added = false;
                }
                repaint();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
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
    }

    private void remove(final IRoutePoint point) {
        if (model.contains(point)) {
            removed = true;
            model.removeElement(point);
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

    private class RoutePointRenderer extends JPanel implements ListCellRenderer<IRoutePoint> {
        private static final long serialVersionUID = 1L;

        private final JLabel indexLabel;
        private final JLabel addressLabel;
        private final JButton button;

        public RoutePointRenderer() {
            setPreferredSize(new Dimension(158, CELL_SIZE));
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
            indexLabel = new JLabel() {
                private static final long serialVersionUID = 1L;
                private final int charWidth = getFontMetrics(getFont()).stringWidth("9") + 1;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension((int) (Math.log10(model.size()) + 2) * charWidth + 2, 16);
                }
            };
            indexLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            addressLabel = new JLabel() {
                private static final long serialVersionUID = 1L;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(RoutePointRenderer.this.getWidth() - indexLabel.getWidth() - 16 - 10, 16);
                }
            };

            button = new JButton("x");
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setPreferredSize(new Dimension(16, 16));
            button.setOpaque(false);
            button.setFocusable(false);

            add(indexLabel);
            add(addressLabel);
            add(button);
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
            // TODO getTargetIndex
            indexLabel.setText((value.getListIndex() + 1) + ". ");
            addressLabel.setText(value.getAddress());
            switch (value.getState()) {
                case added:
                    if (hoveredIndex == index) {
                        setBackground(HOVERED_COLOR);
                        button.setVisible(true);
                    } else {
                        setBackground(NEUTRAL_COLOR);
                        button.setVisible(false);
                    }
                    break;
                case editing:
                    setBackground(SELECTED_COLOR);
                    button.setVisible(true);
                    break;
                default:
                    break;
            }

            return this;
        }
    }
}
