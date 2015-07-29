package com.jetbrains.jetpad.vclang;

import com.jetbrains.jetpad.vclang.module.*;
import com.jetbrains.jetpad.vclang.serialization.ModuleDeserialization;
import com.jetbrains.jetpad.vclang.term.definition.ClassDefinition;
import com.jetbrains.jetpad.vclang.term.error.TypeCheckingError;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ConsoleMain {
  public static void main(String[] args) {
    Options cmdOptions = new Options();
    cmdOptions.addOption("h", "help", false, "print this message");
    cmdOptions.addOption(Option.builder("s").longOpt("source").hasArg().argName("dir").desc("source directory").build());
    cmdOptions.addOption(Option.builder("o").longOpt("output").hasArg().argName("dir").desc("output directory").build());
    cmdOptions.addOption(Option.builder("L").hasArg().argName("dir").desc("add <dir> to the list of directories searched for libraries").build());
    cmdOptions.addOption(Option.builder().longOpt("recompile").desc("recompile files").build());
    CommandLineParser cmdParser = new DefaultParser();
    CommandLine cmdLine;
    try {
      cmdLine = cmdParser.parse(cmdOptions, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      return;
    }

    if (cmdLine.hasOption("h")) {
      new HelpFormatter().printHelp("vclang [FILES]", cmdOptions);
    }

    String sourceDirStr = cmdLine.getOptionValue("s");
    final File sourceDir = new File(sourceDirStr == null ? System.getProperty("user.dir") : sourceDirStr);
    String outputDirStr = cmdLine.getOptionValue("o");
    File outputDir = outputDirStr == null ? null : new File(outputDirStr);
    boolean recompile = cmdLine.hasOption("recompile");

    List<File> libDirs = new ArrayList<>();
    String workingDir = System.getenv("AppData");
    File workingPath = null;
    if (workingDir != null) {
      workingPath = new File(workingDir, "vclang");
    } else {
      workingDir = System.getProperty("user.home");
      if (workingDir != null) {
        workingPath = new File(workingDir, ".vclang");
      }
    }
    if (cmdLine.getOptionValues("L") != null) {
      for (String dir : cmdLine.getOptionValues("L")) {
        libDirs.add(new File(dir));
      }
    }
    if (workingPath != null) {
      libDirs.add(new File(workingPath, "lib"));
    }

    final ModuleLoader moduleLoader = new ModuleLoader();
    ModuleDeserialization moduleDeserialization = new ModuleDeserialization(moduleLoader);
    moduleLoader.init(new FileSourceSupplier(moduleLoader, sourceDir), new FileOutputSupplier(moduleDeserialization, outputDir, libDirs), recompile);

    if (cmdLine.getArgList().isEmpty()) {
      if (sourceDirStr == null) return;
      try {
        Files.walkFileTree(sourceDir.toPath(), new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
            if (path.getFileName().toString().endsWith(".vc")) {
              processFile(moduleLoader, path, sourceDir);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
            System.err.println(ModuleError.ioError(e));
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        System.err.println(ModuleError.ioError(e));
      }
    } else {
      for (String fileName : cmdLine.getArgList()) {
        processFile(moduleLoader, Paths.get(fileName), sourceDir);
      }
    }
  }

  static private List<String> getModule(Path file) {
    int nameCount = file.getNameCount();
    if (nameCount < 1) return null;
    List<String> names = new ArrayList<>(nameCount);
    for (int i = 0; i < nameCount; ++i) {
      String name = file.getName(i).toString();
      if (i == nameCount - 1) {
        if (!name.endsWith(".vc")) return null;
        name = name.substring(0, name.length() - 3);
      }

      if (name.length() == 0 || !(Character.isLetterOrDigit(name.charAt(0)) || name.charAt(0) == '_')) return null;
      for (int j = 1; j < name.length(); ++j) {
        if (!(Character.isLetterOrDigit(name.charAt(j)) || name.charAt(j) == '_' || name.charAt(j) == '-' || name.charAt(j) == '\'')) return null;
      }
      names.add(name);
    }
    return names;
  }

  static private void processFile(ModuleLoader moduleLoader, Path fileName, File sourceDir) {
    Path relativePath = sourceDir != null && fileName.startsWith(sourceDir.toPath()) ? sourceDir.toPath().relativize(fileName) : fileName.getFileName();
    List<String> moduleNames = getModule(relativePath);
    if (moduleNames == null) {
      System.err.println(fileName + ": incorrect file name");
      return;
    }

    ClassDefinition module = moduleLoader.rootModule();
    for (int i = 0; i < moduleNames.size() - 1; ++i) {
      ClassDefinition module1 = module.getClass(moduleNames.get(i), null, moduleLoader.getErrors());
      if (module1 == null) return;
      module1.hasErrors(false);
      module.addStaticField(module1, moduleLoader.getErrors());
      module = module1;
    }
    Module newModule = new Module(module, moduleNames.get(moduleNames.size() - 1));
    moduleLoader.loadModule(newModule, false);

    for (ModuleError moduleError : moduleLoader.getErrors()) {
      System.err.println(moduleError);
    }

    for (TypeCheckingError error : moduleLoader.getTypeCheckingErrors()) {
      System.err.println(error);
    }

    moduleLoader.getErrors().clear();
    moduleLoader.getTypeCheckingErrors().clear();
  }
}
