package controller;

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import model.IApplication;
import model.map.IMap;
import model.targets.IRoutePoint;
import view.IListListener;
import view.ISidebarView;
import controller.stateMachine.IStateMachine;

public class SidebarController {

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
                    final IMap map = application.getMap();
                    map.center(point.getX(map.getZoom()), point.getY(map.getZoom()));
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
        view.addActionListener((e) -> {
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
                // TODO improve
                case "poi enabled":
                    application.getImageLoader().getImageAccessors().get(2).setVisible(true);
                    application.getImageLoader().update();
                    break;
                case "poi disabled":
                    application.getImageLoader().getImageAccessors().get(2).setVisible(false);
                    break;
                case "label enabled":
                    application.getImageLoader().getImageAccessors().get(0).setVisible(true);
                    application.getImageLoader().update();
                    break;
                case "label disabled":
                    application.getImageLoader().getImageAccessors().get(0).setVisible(false);
                    break;
                default:
                    final String[] names = application.getRouteManager().getRouteSolvers();
                    for (int i = 0; i < names.length; i++) {
                        if (names[i].equals(e.getActionCommand())) {
                            machine.setRouteSolver(i);
                            break;
                        }
                    }
            }
        });
    }
}