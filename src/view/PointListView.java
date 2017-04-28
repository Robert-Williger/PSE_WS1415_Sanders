package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import model.targets.IPointList;
import model.targets.IRoutePoint;

public class PointListView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color HOVERED_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_COLOR = Color.LIGHT_GRAY;
    private static final Color NEUTRAL_COLOR = Color.WHITE;

    private final RoutePointListModel model;
    private final RoutePointRenderer renderer;
    private final JList<IRoutePoint> list;
    private int hoveredIndex;

    public PointListView(final IPointList pointList) {
        model = new RoutePointListModel(pointList);
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
        model.setPointList(pointList);
    }

    public void addListListener(final IListListener listener) {
        listenerList.add(IListListener.class, listener);
    }

    private void initialize() {
        final ListTransferHandler handler = new ListTransferHandler(list) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void handleRemove(final int index) {
                fireIndexRemoved(index);
            }

            @Override
            protected void handleChange(final int fromIndex, final int toIndex) {
                fireIndexChanged(fromIndex, toIndex);
            }
        };

        list.setTransferHandler(handler);
        setTransferHandler(handler);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDropMode(DropMode.INSERT);
        list.setCellRenderer(renderer);
        list.setDragEnabled(true);
        list.setFocusable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setBackground(NEUTRAL_COLOR);
        add(list);

        final MouseAdapter listener = new MouseAdapter() {

            @Override
            public void mouseExited(final MouseEvent e) {
                if (isEnabled()) {
                    updateHoveredIndex(-1);
                }
            }

            @Override
            public void mouseMoved(final MouseEvent e) {
                if (isEnabled()) {
                    final int index = e.getY() / 20;
                    updateHoveredIndex(index < model.getSize() ? index : -1);
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isEnabled()) {
                    final int index = e.getY() / 20;
                    if (index < model.getSize()) {
                        final JButton button = renderer.button;
                        if (!button.contains(e.getX() - button.getX(), e.getY() - index * 20 - button.getY())) {
                            fireIndexClicked(index, e);
                        }
                    }
                }
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isEnabled()) {
                    final int index = e.getY() / 20;
                    if (index < model.getSize()) {
                        final JButton button = renderer.button;
                        if (button.contains(e.getX() - button.getX(), e.getY() - index * 20 - button.getY())) {
                            fireIndexRemoved(index);
                        }
                    }
                }

            }
        };

        list.addMouseMotionListener(listener);
        list.addMouseListener(listener);

        // TODO bug occurs, if point list exited by listButtons.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(final MouseEvent e) {
                if (isEnabled()) {
                    updateHoveredIndex(-1);
                }
            }
        });

        hoveredIndex = -1;
    }

    private void updateHoveredIndex(final int index) {
        if (index != hoveredIndex) {
            final int oldHover = hoveredIndex;
            hoveredIndex = index;
            model.fireContentChanged(oldHover);
            model.fireContentChanged(index);
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
            setPreferredSize(new Dimension(162, 20));
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
            indexLabel = new JLabel() {
                private static final long serialVersionUID = 1L;
                private final int charWidth = getFontMetrics(getFont()).stringWidth("9") + 1;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension((int) (Math.log10(model.getSize()) + 2) * charWidth, 16);
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

            button = new JButton("x") {
                private static final long serialVersionUID = 1L;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(16, 16);
                }
            };
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setOpaque(false);

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
