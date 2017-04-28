import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import model.targets.IPointList;
import model.targets.IRoutePoint;

public class RoutePointTableModel implements TableModel {

    private final IPointList list;

    public RoutePointTableModel(final IPointList list) {
        this.list = list;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final IRoutePoint point = list.get(rowIndex);
        return columnIndex == 0 ? point.getListIndex() + "." : point.getAddress();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        // TODO Auto-generated method stub

    }

}
