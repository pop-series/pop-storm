package io.pop.storm.recipes.spring.beans.iface;

import java.util.List;
import java.util.function.Function;

public interface WordExtractor extends Function<String, List<String>> {}
