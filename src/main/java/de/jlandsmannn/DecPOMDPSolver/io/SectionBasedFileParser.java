package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SectionBasedFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(SectionBasedFileParser.class);

  protected ISectionParser sectionParser;
  protected SectionKeyword currentKeyword;
  protected StringBuilder currentSectionBuilder = new StringBuilder();

  protected SectionBasedFileParser(ISectionParser sectionParser) {
    this.sectionParser = sectionParser;
  }

  protected void tryParse(String fileName) throws IOException {
    try (var file = readFile(fileName)) {
      String currentLine = null;
      do {
        currentLine = file.readLine();
        parseLine(currentLine);
      } while (currentLine != null);
    }
    sectionParser.gatherData();
  }

  protected BufferedReader readFile(String fileName) throws IOException {
    return readResourceFile(fileName)
      .or(() -> readNormalFile(fileName))
      .orElseThrow(() -> new FileNotFoundException(fileName));
  }

  private Optional<BufferedReader> readResourceFile(String fileName) {
    try {
      var classLoader = getClass().getClassLoader();
      var url = Optional.ofNullable(classLoader.getResource(fileName))
        .orElseThrow(() -> new FileNotFoundException(fileName));
      var path = Path.of(url.getPath());
      var bufferedReader = Files.newBufferedReader(path);
      return Optional.of(bufferedReader);
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private Optional<BufferedReader> readNormalFile(String fileName) {
    try {
      var fileReader = new FileReader(fileName);
      var bufferedReader = new BufferedReader(fileReader);
      return Optional.of(bufferedReader);
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  protected void parseLine(String currentLine) {
    if (currentLine == null) {
      LOG.info("Reached end of file, finishing current section and parsing it.");
      parseCurrentSection();
      return;
    }
    var keywordMatching = sectionParser.getSectionKeywords().stream()
      .filter(keyword -> keyword.isAtBeginningOf(currentLine))
      .findFirst();

    if (keywordMatching.isPresent()) {
      LOG.debug("Found keyword at beginning of line, finishing current section and parsing it.");
      parseCurrentSection();
      startNewSection(keywordMatching.get());
      currentSectionBuilder.append(currentLine.trim());
      return;
    }
    LOG.debug("No keyword matched current line, adding line to current section.");
    if (!currentSectionBuilder.isEmpty()) {
      currentSectionBuilder.append(System.lineSeparator());
    }
    currentSectionBuilder.append(currentLine.trim());
  }

  protected void parseCurrentSection() {
    sectionParser.parseSection(currentKeyword, currentSectionBuilder.toString());
  }

  protected void startNewSection(SectionKeyword keyword) {
    currentSectionBuilder = new StringBuilder();
    currentKeyword = keyword;
  }


}
