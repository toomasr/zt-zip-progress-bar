package com.toomasr.zip.progressbar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

public class Main {
  // the number of tmp files that are created for this demo
  private static final int NO_FILES = 100;
  // the size of a single file, change the first number to the
  // number of megabytes a file should be, by default 1 MB
  private static final long FILE_SIZE = 1 * 1024 * 1024;
  
  
  // internal variables
  private static final Random random = new Random();
  
  private static final int BUFFER_SIZE = 8192;
  
  private static final File targetDir = new File("target");
  private static final File tmpDir = new File(targetDir, "tmpDir");
  private static final File zipArchive = new File(targetDir, "tmpDir.zip");

  public static void main(String[] args) throws Exception {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "WARN");

    produceBunchOfFiles();

    if (zipArchive.exists()) {
      zipArchive.delete();
    }

    // I'm using a set to keep track of how many files have been
    // processed already, as I work in an anonymous class and I
    // can't reference a non final variable - other solutions
    // are class/object variables/fields
    Set<String> progress = new HashSet<String>();

    ZipUtil.pack(tmpDir, zipArchive, new NameMapper() {
      @Override
      public String map(String name) {
        progress.add(name);
        String progressStr = generateProgressStr(progress, 30);
        // the carriage return will return the cursor to the
        // start of the line
        System.out.print("|"+progressStr+"\r");
        return name;
      }
    });
    System.out.print("\rDone                                                 \n");
  }

  /**
   * Generates a string consisting a % number of # chars that represents
   * the progress. It also adds a % number in the end after a | char.
   * 
   * @param progress a set where the size designates the number of processed archives
   * @param length the maximum length of the progressbar
   * @return a string you can print out for the progressbar efect
   */
  private static String generateProgressStr(Set<String> progress, int length) {
    StringBuffer rtrn = new StringBuffer();
    
    double percentage = progress.size()/(double)NO_FILES;
    int size = (int)(length * percentage);
    for (int i = 0; i < size; i++) {
      rtrn.append("#");
    }
    
    for (int i = length; i >= size; i--) {
      rtrn.append(" ");
    }
    
    String rtrnString = rtrn.toString() + "| " + (int)(percentage*100)+"%";
    return rtrnString;
  }
  
  public static void produceBunchOfFiles() throws IOException {

    if (!tmpDir.exists()) {
      tmpDir.mkdirs();
    }

    for (int i = 0; i < NO_FILES; i++) {
      String fileName = generateFileName(i);
      File tmpFile = new File(tmpDir, fileName);

      if (tmpFile.exists()) {
        continue;
      }

      try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));) {
        for (int j = 0; j < FILE_SIZE / BUFFER_SIZE; j++) {
          byte[] bytes = new byte[BUFFER_SIZE];
          random.nextBytes(bytes);
          bos.write(bytes);
        }
      }
    }
  }

  private static String generateFileName(int i) {
    String fileName = "tmpFile-";
    if (i < 10) {
      fileName = fileName + "00" + i;
    }
    else if (i < 100) {
      fileName = fileName + "0" + i;
    }
    else {
      fileName = fileName + i;
    }
    return fileName + ".bin";
  }
}
