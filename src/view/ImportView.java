package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class ImportView extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JProgressBar progressBar;
    private final JLabel info;

    public ImportView() {
        progressBar = new JProgressBar(0, 100);
        add(progressBar, BorderLayout.CENTER);

        final String[] location = {BorderLayout.EAST, BorderLayout.WEST, BorderLayout.SOUTH};
        for (final String element : location) {
            final JPanel dummy = new JPanel();
            dummy.setPreferredSize(new Dimension(20, progressBar.getPreferredSize().height));
            add(dummy, element);
        }

        info = new JLabel("Lade Kartendaten...");
        info.setPreferredSize(new Dimension(100, 30));
        info.setHorizontalAlignment(SwingConstants.CENTER);
        add(info, BorderLayout.NORTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setModalityType(ModalityType.TOOLKIT_MODAL);
        setTitle("Karte wird importiert");
        pack();
    }

    public void setProgress(final int progress) {
        progressBar.setValue(progress);
    }

    public void setStep(final String step) {
        info.setText(step);
    }
}
