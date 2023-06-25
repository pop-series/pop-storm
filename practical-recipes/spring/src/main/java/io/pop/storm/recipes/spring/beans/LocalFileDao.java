package io.pop.storm.recipes.spring.beans;

import io.pop.storm.recipes.spring.beans.iface.FileDao;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LocalFileDao implements FileDao {
  @Getter private final String filePath;
  @Getter private int totalLines;
  private List<String> lines;

  public void init() {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(filePath)))) {
      lines = reader.lines().collect(Collectors.toList());
      totalLines = lines.size();
      log.info("successfully loaded file: [{}] with [{}] lines", filePath, totalLines);
    } catch (IOException e) {
      log.warn("unable to read given file: [{}]", filePath, e);
      lines = Collections.emptyList();
    }
  }

  @Override
  public String getLine(int index) {
    return lines.get(index);
  }
}
