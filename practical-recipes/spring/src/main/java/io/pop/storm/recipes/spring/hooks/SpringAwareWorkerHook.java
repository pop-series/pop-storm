package io.pop.storm.recipes.spring.hooks;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.hooks.BaseWorkerHook;
import org.apache.storm.task.WorkerTopologyContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Slf4j
public class SpringAwareWorkerHook extends BaseWorkerHook {

  private final String[] springConfigLocations;

  private transient ConfigurableApplicationContext springContext;

  public SpringAwareWorkerHook(final String[] springConfigLocations) {
    super();
    this.springConfigLocations = springConfigLocations;
  }

  @Override
  public void start(Map<String, Object> topoConf, WorkerTopologyContext context) {
    super.start(topoConf, context);
    springContext = new ClassPathXmlApplicationContext(springConfigLocations);
    // NOTE: using BeanFactoryProvider for sharing spring context in a singleton is a temporary hack
    // which may no longer be required from storm v2.5.x onwards.
    // Refer https://issues.apache.org/jira/browse/STORM-3925 for more details.
    BeanFactoryProvider.setFactory(springContext);
    log.info("spring context initialized: [{}]", springContext);
  }

  @Override
  public void shutdown() {
    super.shutdown();
    if (springContext != null) {
      springContext.close();
    }
  }
}
