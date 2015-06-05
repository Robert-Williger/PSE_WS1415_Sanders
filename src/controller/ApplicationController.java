package controller;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.IApplication;
import model.routing.IProgressListener;
import view.FileChooserView;
import view.IApplicationView;
import view.ImportView;
import controller.stateMachine.IStateMachine;
import controller.stateMachine.StateMachine;

public class ApplicationController extends AbstractController<IApplicationView> {

    private final IApplication model;
    private final IApplicationView view;
    private final ImportView importView;
    private final IStateMachine machine;

    private final FileChooserView importer;
    private final FileChooserView exporter;

    public ApplicationController(final IApplicationView view, final IApplication model) {
        this.model = model;
        this.view = view;
        machine = new StateMachine(model, view);
        importView = new ImportView();

        Image image = null;
        try {
            image = ImageIO.read(getClass().getResource("import.png"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        importer = new FileChooserView(".", image);

        try {
            image = ImageIO.read(getClass().getResource("export.png"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        exporter = new FileChooserView(image);

        initialize(view, model);
    }

    private void initialize(final IApplicationView view, final IApplication model) {
        new MapController(view.getMap(), model, machine);
        new SidebarController(view.getSidebar(), model, machine);

        model.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int progress) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        importView.setProgress(progress);
                    }
                });
            }

            @Override
            public void errorOccured(final String message) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, message, "Fehler beim Import", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        view.addMenuBarListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                switch (e.getActionCommand()) {
                    case "export":
                        controlExport();
                        break;
                    case "import":
                        controllImport();
                        break;
                    case "exit":
                        controllExit();
                        break;
                    case "help":
                        controllHelp();
                        break;
                }
            }

        });

        view.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                controllExit();
            }

        });

        importView.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                model.cancelCalculation();
            }

        });

        importer.setAcceptAllFileFilterUsed(false);
        importer.setDialogTitle("Kartendatei auswählen");
        importer.setFileFilter(new FileNameExtensionFilter("MAP (*.map)", "map"));

        exporter.setAcceptAllFileFilterUsed(false);
        exporter.setDialogTitle("Speichern unter");
        exporter.setFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));

        final File file = new File("default.map");
        if (file.exists()) {
            importMap(file);
        }
    }

    protected void controlExport() {
        final int returnVal = exporter.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = exporter.getSelectedFile();
            final String filename = exporter.getSelectedFile().toString();
            if (!filename.endsWith(".png")) {
                file = new File(filename + ".png");
            }
            try {
                ImageIO.write((RenderedImage) view.getMap().createScreenshot(), "png", file);
            } catch (final IOException e) {
                JOptionPane.showConfirmDialog(null, "Beim Exportieren der Karte ist ein Fehler aufgetreten.", "Fehler",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void controllImport() {
        final int returnVal = importer.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = importer.getSelectedFile();
            if (file.exists() && importer.accept(file)) {
                machine.cancel();
                machine.resetPoints();
                importMap(file);
            }
        }
    }

    private void importMap(final File file) {
        new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        importView.setProgress(0);
                        importView.setLocationRelativeTo(null);
                        importView.setVisible(true);
                    }

                });

                if (model.setMapData(file)) {
                    model.getMap().setViewSize(view.getMap().getSize());
                    model.getImageLoader().update();
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        importView.setVisible(false);
                    }

                });
            }
        }.start();
    }

    protected void controllExit() {
        final int result = JOptionPane.showConfirmDialog(null, "Wirklich beenden?", "Programm beenden",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }

    protected void controllHelp() {
        view.setHelpVisible(true);
    }
}