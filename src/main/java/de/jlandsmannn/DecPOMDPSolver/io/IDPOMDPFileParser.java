package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This class handles the parsing of a .idpomdp file,
 * by reading the file line by line and stacking those lines
 * until a new section is found, so the former one is complete.
 * When this happens, the former section is parsed by the {@link IDPOMDPSectionParser}.
 * At the end of the file, the current section is also parsed,
 * as it can be seen as complete.
 * The given filename can be a path either inside the resources directory
 * or relative to the directory, where the program is executed.
 */
public class IDPOMDPFileParser<BUILDER extends IsomorphicDecPOMDPWithStateControllerBuilder> extends SectionBasedFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(IDPOMDPFileParser.class);

  protected BUILDER builder;
  protected IDPOMDPSectionParser<BUILDER> sectionParser;

  public static Optional<IsomorphicDecPOMDPWithStateControllerBuilder> parseDecPOMDP(String fileName) {
    var parser = new IDPOMDPFileParser<>(new IsomorphicDecPOMDPWithStateControllerBuilder());
    return parser.parse(fileName);
  }

  protected IDPOMDPFileParser(BUILDER builder) {
    super(new IDPOMDPSectionParser<>(builder));
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
