package org.example;

import java.awt.EventQueue;

public class Main {
    public static void main(String[] args) {
        // Create and show GUI on the Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            DealershipGUI gui = new DealershipGUI();
            gui.setVisible(true);
        });
    }
}

// Author: Sandip Poudel
