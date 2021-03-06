package com.github.lawena.os;

import static com.github.lawena.util.Util.startProcess;
import static com.github.lawena.util.Util.startUncheckedProcess;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import net.tomahawk.ExtensionsFilter;
import net.tomahawk.XFileDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Consumer;
import com.github.lawena.util.Util;

public abstract class WindowsInterface extends OSInterface {

  static final Logger log = LoggerFactory.getLogger(WindowsInterface.class);
  private static final String STEAMPATH_REGISTRY_KEY = "HKEY_CURRENT_USER\\Software\\Valve\\Steam"; //$NON-NLS-1$
  private static final String STEAMPATH_REGISTRY_VALUE = "SteamPath"; //$NON-NLS-1$

  @Override
  public ProcessBuilder getBuilderSteamLaunch(String steamPath) {
    return new ProcessBuilder(steamPath + "/steam.exe"); //$NON-NLS-1$
  }

  @Override
  public Path getSteamPath() {
    return Paths.get(regQuery(STEAMPATH_REGISTRY_KEY, STEAMPATH_REGISTRY_VALUE));
  }

  @SuppressWarnings("nls")
  protected static String regQuery(String key, String value) {
    final StringBuilder result = new StringBuilder();
    startProcess(Arrays.asList("reg", "query", key, "/v", value), new Consumer<String>() {

      @Override
      public void consume(String line) {
        result.append(line + '\n');
      }
    });
    try {
      if (result.lastIndexOf("0x") > 0) {
        return result.substring(result.lastIndexOf("0x") + 2,
            result.indexOf("\n", result.lastIndexOf("0x")));
      } else {
        String str = "REG_SZ    ";
        return result.substring(result.lastIndexOf(str) + str.length(),
            result.indexOf("\n", result.lastIndexOf(str)));
      }
    } catch (IndexOutOfBoundsException e) {
      log.debug("Data not found at value {} for key {}", value, key);
      return "";
    }
  }

  @SuppressWarnings("nls")
  @Override
  public void openFolder(Path dir) {
    try {
      startUncheckedProcess(Arrays.asList("explorer.exe", "/select," + dir.toString()));
    } catch (IOException | InterruptedException e) {
      // fallback to Java desktop API
      super.openFolder(dir);
    }
  }

  @SuppressWarnings("nls")
  static void closeHandle(String pid, String handle) {
    String tool = Paths.get("lwrt/tools/handle/handle.exe").toAbsolutePath().toString();
    startProcess(Arrays.asList(tool, "-c", handle, "-p", pid, "-y"), new Consumer<String>() {

      int count = 0;

      @Override
      public void consume(String line) {
        if (count > 7) {
          log.debug("[handle] {}", line);
        }
        count++;
      }
    });
  }

  @SuppressWarnings("nls")
  @Override
  public void closeHandles(Path path) {
    String tool = Paths.get("lwrt/tools/handle/handle.exe").toAbsolutePath().toString();
    startProcess(Arrays.asList(tool, path.toString()), new Consumer<String>() {

      int count = 0;

      @Override
      public void consume(String line) {
        if (count > 4) {
          String[] columns = line.split("[ ]+type: [A-Za-z]+[ ]+|: |[ ]+pid: ");
          if (columns.length == 4) {
            log.debug("[handle] Closing handle {} opened by {}", columns[3], columns[0]);
            closeHandle(columns[1], columns[2]);
          } else {
            log.debug("[handle] {}", line);
          }
        }
        count++;
      }
    });
  }

  @SuppressWarnings("nls")
  @Override
  public void delete(Path path) {
    startProcess(Arrays.asList("cmd", "/c", "del", "/f", "/s", "/q", "/a", path.toString()),
        new Consumer<String>() {

          @Override
          public void consume(String line) {
            log.debug("[delete] {}", line);
          }
        });
  }

  @Override
  public String getVTFCmdLocation() {
    return Paths.get("lwrt/tools/vtfcmd/VTFCmd.exe").toAbsolutePath().toString(); //$NON-NLS-1$
  }

  @Override
  public String chooseSingleFolder(Frame parent, String title, String path) {
    if (parent == null) {
      log.warn("Parent frame must not be null to use native dialog"); //$NON-NLS-1$
      return super.chooseSingleFolder(null, title, path);
    } else {
      XFileDialog dialog = new XFileDialog(parent);
      dialog.setTitle(title);
      dialog.setDirectory(path);
      String folder = dialog.getFolder();
      dialog.dispose();
      return folder;
    }
  }

  @Override
  public String chooseSaveFile(Frame parent, String title, String path,
      List<ExtensionsFilter> filters) {
    if (parent == null) {
      log.warn("Parent frame must not be null to use native dialog"); //$NON-NLS-1$
      return super.chooseSaveFile(null, title, path, filters);
    } else {
      XFileDialog dialog = new XFileDialog(parent);
      dialog.setTitle(title);
      dialog.addFilters(filters);
      String content = dialog.getSaveFile();
      String folder = dialog.getDirectory();
      dialog.dispose();
      return (content == null ? content : new File(folder, content).toString());
    }
  }

  @Override
  public String chooseSingleFile(Frame parent, String title, String path,
      List<ExtensionsFilter> filters) {
    if (parent == null) {
      log.warn("Parent frame must not be null to use native dialog"); //$NON-NLS-1$
      return super.chooseSingleFile(null, title, path, filters);
    } else {
      XFileDialog dialog = new XFileDialog(parent);
      dialog.setTitle(title);
      dialog.addFilters(filters);
      String content = dialog.getFile();
      String folder = dialog.getDirectory();
      dialog.dispose();
      return (content == null ? content : new File(folder, content).toString());
    }
  }

  @SuppressWarnings("nls")
  @Override
  public boolean isSymbolicLink(Path path) {
    if (Files.isSymbolicLink(path)) {
      return true;
    } else {
      int ret =
          Util.startProcess(Arrays.asList("cmd", "/c", "fsutil", "reparsepoint", "query", path
              .toAbsolutePath().toString()));
      return ret == 0;
    }
  }
}
