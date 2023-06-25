package io.pop.storm.recipes.spring.beans;

import io.pop.storm.recipes.spring.beans.iface.WordExtractor;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SplitSentence implements WordExtractor {
  private final Pattern boundaryPattern;

  public SplitSentence(final String boundaryRegex) {
    boundaryPattern = Pattern.compile(boundaryRegex);
  }

  @Override
  public List<String> apply(String s) {
    return boundaryPattern.splitAsStream(s).collect(Collectors.toList());
  }
}
