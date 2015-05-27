package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import model.IApplication;
import model.targets.IRoutePoint;
import view.IListListener;
import view.ISidebarView;
import controller.stateMachine.IStateMachine;

public class SidebarController extends AbstractController<ISidebarView> {

    private final IApplication application;

    private final IStateMachine machine;

    public SidebarController(final ISidebarView view, final IApplication application, final IStateMachine machine) {
        this.application = application;
        this.machine = machine;

        initialize(view);
    }

    private void initialize(final ISidebarView view) {
        view.addListListener(new IListListener() {

            @Override
            public void indexChanged(final int fromIndex, final int toIndex) {
                machine.changeOrder(fromIndex, toIndex);
            }

            @Override
            public void indexClicked(final int index, final MouseEvent e) {
                final IRoutePoint point = application.getRouteManager().getPointList().get(index);
                if (e.getClickCount() == 1) {
                    machine.selectPoint(point);
                } else {
                    application.getMap().center(point.getStreetNode().getLocation());
                    application.getImageLoader().update();
                }
            }

            @Override
            public void indexRemoved(final int index) {
                machine.removePoint(application.getRouteManager().getPointList().get(index));
            }

        });
        view.addTextFieldListener(new DocumentListener() {

            @Override
            public void insertUpdate(final DocumentEvent e) {
                updateText(e);
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                updateText(e);
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                updateText(e);
            }

            private void updateText(final DocumentEvent e) {
                final Document document = e.getDocument();
                try {
                    final String address = document.getText(0, document.getLength());
                    machine.setAddressText(address);
                } catch (final BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

        });
        view.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                switch (e.getActionCommand()) {
                    case "search point":
                        machine.searchPoint();
                        break;
                    case "confirm":
                        machine.confirm();
                        break;
                    case "add point":
                        machine.addPoint();
                        break;
                    case "move up":
                        machine.movePointUp();
                        break;
                    case "move down":
                        machine.movePointDown();
                        break;
                    case "cancel":
                        machine.cancel();
                        break;
                    case "reset":
                        final int result = JOptionPane.showConfirmDialog(null, "Alle Punkte unwiderruflich löschen?",
                                "Routenpunkte löschen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            machine.resetPoints();
                        }
                        break;
                    case "start calculation":
                        machine.startCalculation();
                        break;
                    case "cancel calculation":
                        machine.cancelCalculation();
                        break;
                    case "tsp enabled":
                        machine.setTSPEnabled(true);
                        break;
                    case "tsp disabled":
                        machine.setTSPEnabled(false);
                        break;
                    case "poi enabled":
                        application.getImageLoader().getPOIAccessor().setVisible(true);
                        break;
                    case "poi disabled":
                        application.getImageLoader().getPOIAccessor().setVisible(false);
                        break;
                }
            }
        });
    }
}