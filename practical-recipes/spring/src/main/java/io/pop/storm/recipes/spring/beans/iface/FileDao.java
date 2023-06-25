package io.pop.storm.recipes.spring.beans.iface;

public interface FileDao {
  String getFilePath();

  int getTotalLines();

  String getLine(int index);
}
