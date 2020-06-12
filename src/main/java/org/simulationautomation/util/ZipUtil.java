package org.simulationautomation.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

  private static final Logger log = LoggerFactory.getLogger(ZipUtil.class);


  /**
   * Zip recursively all directories and files starting from the given directory</br>
   * Destination will be the same path
   * 
   * @param directoryPath which has to be zipped
   * @return path to zip file
   */
  public String createZipFileRecursively(String directoryPath, String destinationPath) {
    // try with resources - creating outputstream and ZipOutputSttream
    try (FileOutputStream fos = new FileOutputStream(destinationPath.concat(".zip"));
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


      return destinationPath.concat(".zip");
    } catch (IOException e) {
      log.error("Could not zip file at specified directoy path=" + directoryPath, e);
      return null;
    }
  }
}
