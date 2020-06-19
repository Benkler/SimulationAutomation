package org.simulationautomation.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

  private static final Logger log = LoggerFactory.getLogger(FileUtil.class);


  /**
   * Delete file at specified path
   * 
   * @param path
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



  /**
   * Load file into String
   * 
   * @param path of the file
   * @return
   */
  public static String loadFileAsString(String path) {

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



    return byteArrayOutputStream.toString();

  }


  /**
   * Load file into byte Array
   * 
   * @param path of the file
   * @return
   */
  public static byte[] loadFileAsByteStream(String path) {


    return loadFileAsString(path).getBytes();

  }

  /**
   * Creates (or replace) file at specified path with given content.
   * 
   * @param path
   * @param content
   */
  public static boolean createFileFromString(String path, String content) {


    PrintWriter out = null;
    try {
      File metadataFile = new File(path);
      metadataFile.createNewFile();
      out = new PrintWriter(metadataFile);

      out.println(content);
      log.info("Successfully create file from string at path=" + path);
      return true;
    } catch (IOException e) {

      log.error("Error while creating file at path=" + path);
      return false;

    } finally {
      if (out != null) {
        out.close();
      }
    }



  }


}
