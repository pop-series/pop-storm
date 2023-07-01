package io.pop.storm.recipes.spring.hooks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;

public class BeanFactoryProvider {

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private static BeanFactory factory;
}
