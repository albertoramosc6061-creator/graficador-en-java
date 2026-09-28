package mathgraph;

import mathgraph.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set cross-platform look and feel or system default for crisp font rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
