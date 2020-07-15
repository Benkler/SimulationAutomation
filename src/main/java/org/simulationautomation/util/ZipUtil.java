package org.simulationautomation.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

  private static final int BUFFER = 512;
  private static final long TOOBIG = 0x6400000; // Max size of unzipped data, 100MB
  private static final int TOOMANY = 1024; // Max number of files

  private static final Logger log = LoggerFactory.getLogger(ZipUtil.class);
  private static ZipUtil INSTANCE;

  private ZipUtil() {

  }

  public static ZipUtil getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ZipUtil();
    }
    return INSTANCE;
  }


  public final List<String> getFileNamesFromZipFileWithExtension(byte[] zipAsByteArray,
      String extension) throws IOException {
    log.info("Extracting file names from zip file with give extension: " + extension);
    List<String> fileNames = new ArrayList<>();
    InputStream is = new ByteArrayInputStream(zipAsByteArray);
    ZipInputStream zis = new ZipInputStream(is);

    ZipEntry entry = null;
    while ((entry = zis.getNextEntry()) != null) {
      String entryName = entry.getName();
      if (entryName.endsWith(extension)) {
        log.info("File found: " + entryName);
        fileNames.add(entryName);
      }
    }

    return fileNames;
  }

  /**
   * Extract ZipFile (provided as byte array) to destination directory
   * 
   * @param zipAsByteArray
   * @param pathToDir without tailing "/"
   * @throws java.io.IOException
   */
  public final void extractZipToDestinationDir(byte[] zipAsByteArray, String pathToDir)
      throws IOException {
    InputStream is = new ByteArrayInputStream(zipAsByteArray);
    ZipInputStream zis = new ZipInputStream(is);
    ZipEntry entry;
    int entries = 0;
    long total = 0;
    try {
      while ((entry = zis.getNextEntry()) != null) {
        log.info("Extracting: " + entry);
        int count;
        byte data[] = new byte[BUFFER];
        // Write the files to the disk, but ensure that the filename is valid,
        // and that the file is not insanely big
        String zipEntryName = validateFilename(entry.getName(), ".");

        String filePath = pathToDir + zipEntryName;
        if (entry.isDirectory()) {
          log.info("Creating directory " + filePath);
          new File(filePath).mkdir();
          continue;
        }
        log.info("Creating file: " + filePath);
        File outputFile = new File(filePath);
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
        while (total + BUFFER <= TOOBIG && (count = zis.read(data, 0, BUFFER)) != -1) {
          dest.write(data, 0, count);
          total += count;
        }
        dest.flush();
        dest.close();
        zis.closeEntry();
        entries++;
        if (entries > TOOMANY) {
          throw new IllegalStateException("Too many files to unzip.");
        }
        if (total + BUFFER > TOOBIG) {
          throw new IllegalStateException("File being unzipped is too big.");
        }
      }
    } finally {
      zis.close();
    }
  }

  private String validateFilename(String filename, String intendedDir) throws java.io.IOException {
    File f = new File(filename);
    String canonicalPath = f.getCanonicalPath();

    File iD = new File(intendedDir);
    String canonicalID = iD.getCanonicalPath();

    if (canonicalPath.startsWith(canonicalID)) {
      return canonicalPath;
    } else {
      throw new IllegalStateException("File is outside extraction target directory.");
    }
  }


  /**
   * Zip recursively all directories and files starting from the given directory</br>
   * Destination will be the same path
   * 
   * @param directoryPath which has to be zipped
   * @return path to zip file
   */
  public String createZipFileRecursively(String directoryPath, String pathToZipFile) {
    // try with resources - creating outputstream and ZipOutputStream
    try (FileOutputStream fos = new FileOutputStream(pathToZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      Path sourcePath = Paths.get(directoryPath);
      // using WalkFileTree to traverse directory
      Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
            throws IOException {
          // it starts with the source folder so skipping that
          if (!sourcePath.equals(dir)) {
            zos.putNextEntry(new ZipEntry(sourcePath.relativize(dir).toString() + "/"));

            zos.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
            throws IOException {
          zos.putNextEntry(new ZipEntry(sourcePath.relativize(file).toString()));
          Files.copy(file, zos);
          zos.closeEntry();
          return FileVisitResult.CONTINUE;
        }

      });


      return pathToZipFile;
    } catch (IOException e) {
      log.error("Could not zip file at specified directoy path=" + directoryPath, e);
      return null;
    }
  }
}
