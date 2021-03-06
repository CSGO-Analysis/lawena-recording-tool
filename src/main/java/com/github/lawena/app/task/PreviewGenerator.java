package com.github.lawena.app.task;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;
import com.github.lawena.app.Lawena;
import com.github.lawena.app.Tasks;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.profile.Key;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.util.Util;

public class PreviewGenerator extends SwingWorker<Void, Void> {

  private static final Logger log = LoggerFactory.getLogger(PreviewGenerator.class);
  private static final Logger status = LoggerFactory.getLogger("status"); //$NON-NLS-1$

  private Tasks parent;
  private MainModel model;
  private Lawena presenter;

  private List<String> skyboxNames;
  private String vtfcmd;

  public PreviewGenerator(Tasks tasks, List<String> skyboxNames) {
    this.parent = tasks;
    this.model = tasks.getModel();
    this.presenter = tasks.getPresenter();
    this.skyboxNames = skyboxNames;
  }

  @Override
  protected Void doInBackground() throws Exception {
    parent.setCurrentWorker(this, false);
    setProgress(0);
    try {
      vtfcmd = model.getOsInterface().getVTFCmdLocation();
    } catch (UnsupportedOperationException e) {
      // we can't continue
      log.warn("Aborting generation: {}", e.toString()); //$NON-NLS-1$
      return null;
    }
    int count = 0;
    List<String> boxes = Arrays.asList("lf", "bk", "rt", "ft"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    String folder = Key.gameFolderName.getValue(model.getSettings());
    String inputDir = "lwrt/" + folder + "/skybox/vtf"; //$NON-NLS-1$ //$NON-NLS-2$
    String outputDir = "lwrt/" + folder + "/skybox-preview"; //$NON-NLS-1$ //$NON-NLS-2$
    String format = "png"; //$NON-NLS-1$
    for (String name : skyboxNames) {
      count++;
      setProgress((int) (100 * ((double) count / skyboxNames.size())));
      status.info(Messages.getString("PreviewGenerator.creatingPreview"), name); //$NON-NLS-1$
      if (!model.getSkyboxPreviewStore().getMap().containsKey(name)) {
        List<Path> inputs = withParams(inputDir, name, boxes, ".vtf"); //$NON-NLS-1$
        List<Path> outputs = withParams(outputDir, name, boxes, "." + format); //$NON-NLS-1$
        List<Path> vtfin = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
          Path in = inputs.get(i);
          Path out = outputs.get(i);
          if (!Files.exists(out)) {
            vtfin.add(in);
          }
        }
        if (!vtfin.isEmpty()) {
          generate(vtfin, outputDir, format);
        }
        ImageIcon icon = concatenate(outputs);
        log.debug("Adding skybox preview: {}", name); //$NON-NLS-1$
        model.getSkyboxPreviewStore().getMap().put(name, icon);
        deleteTempFiles(outputs);
      }
    }
    try {
      Files.deleteIfExists(Paths.get(outputDir));
    } catch (IOException e) {
      log.warn("Could not remove temporary skybox preview folder: {}", e.toString()); //$NON-NLS-1$
    }
    model.saveSkyboxData();
    return null;
  }

  private static void deleteTempFiles(List<Path> outputs) {
    for (Path out : outputs) {
      try {
        Files.deleteIfExists(out);
      } catch (IOException e) {
        log.warn("Could not delete some of the temporary skybox files: {}", e.toString()); //$NON-NLS-1$
      }
    }
  }

  @Override
  protected void done() {
    presenter.selectSkyboxFromSettings();
    log.info("Skybox loading complete"); //$NON-NLS-1$
    status.info(StatusAppender.OK, Messages.getString("PreviewGenerator.ready")); //$NON-NLS-1$
    if (!isCancelled()) {
      parent.setCurrentWorker(null, false);
    }
  }

  private static ImageIcon concatenate(List<Path> outputs) {
    int width = 32;
    int height = 32;
    int totalwidth = width * outputs.size();
    BufferedImage image = new BufferedImage(totalwidth, height, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < outputs.size(); i++) {
      Path out = outputs.get(i);
      if (!Files.exists(out)) {
        log.warn("Expected file {} but it does not exist!", out); //$NON-NLS-1$
      } else {
        try {
          File input = out.toFile();
          image.createGraphics().drawImage(
              ImageIO.read(input).getScaledInstance(width, height, Image.SCALE_SMOOTH), i * width,
              0, null);
        } catch (IOException e) {
          log.warn("Could not read image: {}", e.toString()); //$NON-NLS-1$
        }
      }
    }
    return new ImageIcon(image);
  }

  private static List<Path> withParams(String parent, String prefix, List<String> suffixes,
      String extension) {
    List<Path> list = new ArrayList<>();
    for (String s : suffixes) {
      list.add(Paths.get(parent, prefix + s + extension));
    }
    return list;
  }

  private void generate(List<Path> inputs, String outputDir, String ext) {
    try {
      Files.createDirectories(Paths.get(outputDir));
      ProcessBuilder pb = new ProcessBuilder(vtfcmd, "-output", outputDir, "-exportformat", ext); //$NON-NLS-1$ //$NON-NLS-2$
      for (Path in : inputs) {
        pb.command().add("-file"); //$NON-NLS-1$
        pb.command().add(in.toString());
      }
      log.debug("Invoking process: {}", pb.command()); //$NON-NLS-1$
      Process pr = pb.start();
      try (BufferedReader input = Util.newProcessReader(pr)) {
        String line;
        while ((line = input.readLine()) != null) {
          log.trace("[vtfcmd] {}", line); //$NON-NLS-1$
        }
      }
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warn("Problem while generating png from vtf file", e); //$NON-NLS-1$
    }
  }

}
