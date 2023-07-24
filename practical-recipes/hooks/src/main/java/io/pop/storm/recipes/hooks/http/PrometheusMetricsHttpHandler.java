package io.pop.storm.recipes.hooks.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PrometheusMetricsHttpHandler implements HttpHandler {
  private final PrometheusMeterRegistry meterRegistry;

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    log.info("inside handle");
    final String response = meterRegistry.scrape();
    exchange.sendResponseHeaders(200, response.length());
    try (final OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    } catch (final Exception ex) {
      log.warn("unable to scrape meter registry", ex);
    }
  }
}
