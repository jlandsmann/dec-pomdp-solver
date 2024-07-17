package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * This class handles the parsing of a .idpomdp file,
 * by reading the file line by line and stacking those lines
 * until a new section is found, so the former one is complete.
 * When this happens, the former section is parsed by the {@link DPOMDPSectionParser}.
 * At the end of the file, the current section is also parsed,
 * as it can be seen as complete.
 * The given filename can be a path either inside the resources directory
 * or relative to the directory, where the program is executed.
 */
public class IsomorphicDPOMDPFileParser<BUILDER extends IsomorphicDecPOMDPWithStateControllerBuilder> extends SectionBasedFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(IsomorphicDPOMDPFileParser.class);

  protected BUILDER builder;
  protected IsomorphicDPOMDPSectionParser<BUILDER> sectionParser;

  public static Optional<IsomorphicDecPOMDPWithStateControllerBuilder> parseDecPOMDP(String fileName) {
    var parser = new IsomorphicDPOMDPFileParser<>(new IsomorphicDecPOMDPWithStateControllerBuilder());
    return parser.parse(fileName);
  }

  protected IsomorphicDPOMDPFileParser(BUILDER builder) {
    super(new IsomorphicDPOMDPSectionParser<>(builder));
    this.builder = builder;
  }

  public Optional<BUILDER> parse(String fileName) {
    try {
      tryParse(fileName);
      return Optional.of(builder);
    } catch (Exception e) {
      LOG.warn("Could not parse .idpomdp file: {}", fileName, e);
      return Optional.empty();
    }
  }
}
