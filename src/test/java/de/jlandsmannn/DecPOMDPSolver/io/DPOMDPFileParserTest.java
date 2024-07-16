package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DPOMDPFileParserTest {

  @Mock
  private DPOMDPSectionParser<DecPOMDPWithStateControllerBuilder> sectionParser;

  @InjectMocks
  private DPOMDPFileParser<DecPOMDPWithStateControllerBuilder> parser;

  @BeforeEach
  void setUp() {
    parser = spy(new DPOMDPFileParser<>(null));
    parser.sectionParser = sectionParser;
  }

  @Test
  void parseDecPOMDPLine_ShouldStartNewSectionIfLineStartsWithKeyword() {
    when(sectionParser.getSectionKeywords()).thenReturn(Set.of(
      DPOMDPSectionKeyword.AGENTS
    ));
    var expectedKeyword = DPOMDPSectionKeyword.AGENTS;
    var currentLine = "agents: 5";
    parser.parseLine(currentLine);

    verify(parser).startNewSection(expectedKeyword);
  }

  @Test
  void parseLine_ShouldParseDecPOMDPCurrentSectionIfLineStartsWithKeyword() {
    when(sectionParser.getSectionKeywords()).thenReturn(Set.of(
      DPOMDPSectionKeyword.AGENTS
    ));
    var currentLine = "agents: 5";
    parser.parseLine(currentLine);

    verify(parser).parseCurrentSection();
  }

  @Test
  void parseLine_ShouldParseDecPOMDPCurrentSectionIfLineIsNull() {
    String currentLine = null;
    parser.parseLine(currentLine);

    verify(parser).parseCurrentSection();
  }

  @Test
  void parseDecPOMDPLine_ShouldAppendCurrentLineToCurrentSectionIfKeywordOccurs() {
    when(sectionParser.getSectionKeywords()).thenReturn(Set.of(
      DPOMDPSectionKeyword.STATES
    ));
    String currentLine = "states: 5";
    parser.parseLine(currentLine);
    String expectedSection = currentLine;
    String actualSection = parser.currentSectionBuilder.toString();
    assertEquals(expectedSection, actualSection);
  }

  @Test
  void parseDecPOMDPLine_ShouldAppendCurrentLineToCurrentSectionIfNoKeywordOccurs() {
    String previousSection = "states:";
    parser.parseLine(previousSection);
    String currentLine = "uniform";
    parser.parseLine(currentLine);
    String expectedSection = previousSection + System.lineSeparator() + "uniform";
    String actualSection = parser.currentSectionBuilder.toString();
    assertEquals(expectedSection, actualSection);
  }

  @Test
  void parseCurrentSection_ShouldCallParseDecPOMDPSectionWithCurrentKeywordAndCurrentSection() {
    parser.currentKeyword = DPOMDPSectionKeyword.AGENTS;
    parser.currentSectionBuilder = new StringBuilder().append("agents: 5");
    var expectedKeyword = parser.currentKeyword;
    var expectedSection = parser.currentSectionBuilder.toString();
    parser.parseCurrentSection();
    verify(sectionParser).parseSection(expectedKeyword, expectedSection);
  }

  @Test
  void startNewSection_ShouldClearCurrentSectionBuilder() {
    var keyword = DPOMDPSectionKeyword.ACTIONS;
    parser.startNewSection(keyword);

    assertEquals("", parser.currentSectionBuilder.toString());
    assertEquals(keyword, parser.currentKeyword);
  }

}