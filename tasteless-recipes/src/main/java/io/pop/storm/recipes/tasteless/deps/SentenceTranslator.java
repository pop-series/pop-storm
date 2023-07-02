package io.pop.storm.recipes.tasteless.deps;

import io.pop.storm.recipes.tasteless.deps.iface.SentenceTransformer;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.storm.shade.com.google.common.annotations.VisibleForTesting;

public class SentenceTranslator implements SentenceTransformer {
  public record Config(String targetHost, int targetPort) {}

  private static Map<Config, SentenceTranslator> instanceMap = new HashMap<>();

  private final HttpClient client;

  @VisibleForTesting
  SentenceTranslator(final Config config) {
    client =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .proxy(ProxySelector.of(new InetSocketAddress(config.targetHost, config.targetPort)))
            .authenticator(Authenticator.getDefault())
            .build();
  }

  @Override
  public String apply(String s) {
    final HttpRequest request =
        HttpRequest.newBuilder().timeout(Duration.ofSeconds(2)).GET().build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new RuntimeException("failed with " + response.statusCode());
      }
      return response.body();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized SentenceTranslator getInstance(final Config config) {
    return instanceMap.computeIfAbsent(config, key -> new SentenceTranslator(key));
  }

  @VisibleForTesting
  static synchronized SentenceTranslator setInstance(
      final Config config, final SentenceTranslator givenInstance) {
    return instanceMap.put(config, givenInstance);
  }
}
