package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionKeyword;

import java.util.Set;

public interface ISectionParser {

  Set<SectionKeyword> getSectionKeywords();

  void parseSection(SectionKeyword keyword, String section);

  void gatherData();
}
