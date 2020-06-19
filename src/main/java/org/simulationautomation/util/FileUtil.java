package org.simulationautomation.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

  private static final Logger log = LoggerFactory.getLogger(FileUtil.class);


  /*
   * Delete file at specified path
   */
  public static void deleteFile(String path) {
    log.info("Trying to delete file at path=" + path);

    try {
      Files.deleteIfExists(Paths.get(path));
      log.info("Successfully delete file");
    } catch (IOException e) {
      log.info("No such file!");
    }

  }


  /*
   * Load file into byte Array TODO to util class
   */
  public static byte[] loadFileAsByteStream(String path) {

    log.info("Load file at path=" + path);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    File zipFile = new File(path);
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(zipFile);
      org.apache.commons.io.IOUtils.copy(fis, byteArrayOutputStream);
    } catch (IOException e) {

      log.error("Error while reading file.", e);
      return null;
    } finally {
      try {
        // Need to close this as it is a fileInputStream
        fis.close();
      } catch (IOException e) {

      }
    }



    return byteArrayOutputStream.toByteArray();

  }


}
