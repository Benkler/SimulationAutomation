package org.simulationautomation.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating and loading files.
 * 
 * @author Niko Benkler
 *
 */
public class FileUtil {

  private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
  // Singleton
  private static FileUtil INSTANCE;

  private FileUtil() {

  }

  public static FileUtil getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FileUtil();
    }
    return INSTANCE;
  }

  /**
   * Delete file at specified path
   * 
   * @param path
   */
  public void deleteFile(String path) {
    log.info("Trying to delete file at path=" + path);

    try {
      Files.deleteIfExists(Paths.get(path));
      log.info("Successfully delete file");
    } catch (IOException e) {
      log.info("No such file!");
    }

  }

  /**
   * Delete Directory and content at specified path
   * 
   * @param path
   */
  public void deleteDirectory(String pathToSimulation) {
    log.info("Trying to delete directory at path=" + pathToSimulation);
    try {
      FileUtils.deleteDirectory(new File(pathToSimulation));
      log.info("Successfully deleted directory!");
    } catch (IOException e) {
      log.error("Could not delete directory!", e);
    }
  }


  /**
   * Load file into byte Array
   * 
   * @param path of the file
   * @return
   */
  public byte[] loadFileAsByteStream(String path) {

    ByteArrayOutputStream stream = loadFile(path);

    if (stream == null) {
      return null;
    }

    return stream.toByteArray();

  }

  /**
   * Load file into String
   * 
   * @param path of the file
   * @return
   */
  public String loadFileAsString(String path) {


    ByteArrayOutputStream stream = loadFile(path);

    if (stream == null) {
      return null;
    }

    return stream.toString();
  }

  /*
   * Actual load method: Load File into ByteArrayOutputStream
   */
  private ByteArrayOutputStream loadFile(String path) {
    log.info("Load file at path=" + path);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    File file = new File(path);


    try (FileInputStream fis = new FileInputStream(file);) {

      org.apache.commons.io.IOUtils.copy(fis, byteArrayOutputStream);
    } catch (IOException e) {

      log.error("Error while reading file.", e);
      return null;
    }


    return byteArrayOutputStream;

  }

  /**
   * Creates file at specified path with given content.
   * 
   * @param path
   * @param content
   * @return true if successful, false otherwise
   */
  public boolean createFileWithContent(String path, String content) {


    File file = new File(path);
    try (PrintWriter out = new PrintWriter(file);) {

      file.createNewFile();
      out.println(content);
      log.info("Successfully create file from string at path=" + path);
      return true;
    } catch (IOException e) {

      log.error("Error while creating file at path=" + path);
      return false;

    }


  }

  /**
   * Searches for a file in a given directory recursively, which means also to search in all
   * sub-directories.
   * 
   * @param simulationBasePath
   * @param fileName
   * @return null if not found
   */
  public byte[] loadFileFromDirectoryRecursively(String baseDirectoryPath, String fileName) {
    Path start = Paths.get(baseDirectoryPath);
    try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
      List<String> files = stream.filter(Files::isRegularFile).map(String::valueOf)
          .filter(filePath -> filePath.endsWith("/" + fileName)).collect(Collectors.toList());


      if (files.size() > 0) {
        log.info("Amount of files found with name=" + fileName + " :" + files.size()
            + " .Get file at path " + files.get(0));
        return loadFile(files.get(0)).toByteArray();
      } else {
        log.info("No File found with name=" + fileName);
      }

    } catch (IOException e1) {
      log.error("IOException while loading file with name=" + fileName);

    }
    return null;
  }


}
