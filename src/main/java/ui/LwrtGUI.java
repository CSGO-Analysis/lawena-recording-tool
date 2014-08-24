package ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import lwrt.Lawena;
import lwrt.SettingsManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class LwrtGUI {

  private static final Logger log = Logger.getLogger("lawena");

  public static void main(String[] args) throws Exception {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    SettingsManager cfg = new SettingsManager("settings.lwf");
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, final Throwable e) {
        log.log(Level.SEVERE, "Unexpected problem in " + t, e);
      }
    });
    final Lawena lawena = new Lawena(cfg);
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          lawena.start();
        } catch (Exception e) {
          log.log(Level.WARNING, "Problem while running the GUI", e);
        }
      }
    });

  }

}
