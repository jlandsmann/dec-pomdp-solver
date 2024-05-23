package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DPOMDPFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(DPOMDPFileParser.class);

  protected DPOMDPSectionParser sectionParser;
  protected DPOMDPSectionKeyword currentKeyword = DPOMDPSectionKeyword.COMMENT;
  protected StringBuilder currentSectionBuilder = new StringBuilder();

  public DPOMDPFileParser() {
    this(new DPOMDPSectionParser(new DecPOMDPBuilder()));
  }

  public DPOMDPFileParser(DPOMDPSectionParser parser) {
    sectionParser = parser;
  }

  public static Optional<DecPOMDPBuilder> parseDecPOMDP(String fileName) {
    try {
      var parser = new DPOMDPFileParser();
      var decPOMDP = parser.doParseDecPOMDP(fileName);
      return Optional.of(decPOMDP);
    } catch (Exception e) {
      LOG.warn("Could not parse decPOMDP file: {}", fileName, e);
      return Optional.empty();
    }
  }

  protected DecPOMDPBuilder doParseDecPOMDP(String fileName) throws IOException {
    try (var file = readFile(fileName)) {
      String currentLine = null;
      do {
        currentLine = file.readLine();
        parseLine(currentLine);
      } while (currentLine != null);
    }
    return sectionParser.gatherData().getBuilder();
  }

  protected BufferedReader readFile(String fileName) throws IOException {
    var classLoader = getClass().getClassLoader();
    var url = classLoader.getResource(fileName);
    if (url == null) throw new FileNotFoundException(fileName);
    var path = Path.of(url.getPath());
    return Files.newBufferedReader(path);
  }

  protected void parseLine(String currentLine) {
    if (currentLine == null) {
      LOG.info("Reached end of file, finishing current section and parsing it.");
      parseCurrentSection();
      return;
    } else if (DPOMDPSectionKeyword.COMMENT.isAtBeginningOf(currentLine)) {
      LOG.debug("Found comment line, ignoring it.");
      return;
    }
    var keywordMatching = DPOMDPSectionKeyword.ALL
      .stream()
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
    currentSectionBuilder.append(System.lineSeparator()).append(currentLine.trim());
  }

  protected void parseCurrentSection() {
    sectionParser.parseSection(currentKeyword, currentSectionBuilder.toString());
  }

  protected void startNewSection(DPOMDPSectionKeyword keyword) {
    currentSectionBuilder = new StringBuilder();
    currentKeyword = keyword;
  }


}
