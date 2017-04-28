import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import model.map.PixelConverter;
import model.targets.AddressPoint;
import model.targets.IPointList;
import model.targets.PointList;
import model.targets.RoutePoint;

public class EditableListExample extends JFrame {
    private static final long serialVersionUID = 1L;

    public EditableListExample() {
        super("Editable List Example");

        final String[] data = new String[] { "Teststraße 1", "Eppinger Straße 42", "Rintheimerstraße 17", "a", "b", "c",
                "d", "e", "f", "g", "h", "i" };
        final IPointList list = new PointList();
        for (final String name : data) {
            final RoutePoint r1 = new RoutePoint();
            r1.setAddressPoint(new AddressPoint(name, 1, 1, 1, 1, new PixelConverter(2)));
            list.add(r1);
        }

        JTable table = new JTable(new RoutePointTableModel(list));

        // final JTextField field = new JTextField();
        // field.setBorder(null);
        DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
        editor.setClickCountToStart(1);
        table.setDefaultEditor(String.class, editor);

        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setOpaque(false);
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        // rightRenderer.setIns

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, true, true, row, column);
                return this;
            }
        };
        leftRenderer.setFont(leftRenderer.getFont().deriveFont(30f));
        leftRenderer.setOpaque(false);
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        table.setRowHeight(40);

        final TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(rightRenderer);
        columnModel.getColumn(1).setCellRenderer(leftRenderer);
        columnModel.getColumn(0).setPreferredWidth(20);
        columnModel.getColumn(0).setWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);

        table.setRowMargin(15);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setColumnHeader(null);
        scrollTable.setMinimumSize(new Dimension(100, 80));

        getContentPane().add(scrollTable);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        final EditableListExample frame = new EditableListExample();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}