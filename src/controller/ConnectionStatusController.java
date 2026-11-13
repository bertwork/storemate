package controller;

import model.repository.Database;
import view.components.common.FooterBar;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

/** Checks the database every few seconds and updates the status light in the footer. */
public class ConnectionStatusController {

    private static final int CHECK_EVERY_MS = 15_000;

    private final FooterBar view;
    private boolean checking = false; // don't start a check while the previous one is still running

    public ConnectionStatusController(FooterBar view) {
        this.view = view;

        Timer timer = new Timer(CHECK_EVERY_MS, e -> check());
        timer.setInitialDelay(0); // first check right away
        timer.start();
    }

    private void check() {
        if (checking) return;
        checking = true;

        // runs in the background so a slow or missing database never freezes the window
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return Database.isReachable();
            }

            @Override
            protected void done() {
                checking = false;
                try {
                    view.setConnected(get());
                } catch (InterruptedException | ExecutionException e) {
                    view.setConnected(false);
                }
            }
        }.execute();
    }
}