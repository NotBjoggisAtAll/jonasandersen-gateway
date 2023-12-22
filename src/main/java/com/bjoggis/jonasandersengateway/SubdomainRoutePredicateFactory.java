package com.bjoggis.jonasandersengateway;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

public class SubdomainRoutePredicateFactory extends
    AbstractRoutePredicateFactory<SubdomainRoutePredicateFactory.Config> {

  private final Logger logger = LoggerFactory.getLogger(SubdomainRoutePredicateFactory.class);
  private final PathMatcher pathMatcher = new AntPathMatcher(".");


  public SubdomainRoutePredicateFactory() {
    super(SubdomainRoutePredicateFactory.Config.class);
  }

  @Override
  public Predicate<ServerWebExchange> apply(Config config) {
    return (GatewayPredicate) exchange -> {
      InetSocketAddress localAddress = exchange.getRequest().getLocalAddress();

      if (localAddress != null) {
        String match = null;
        String host = localAddress.getHostName();
        for (int i = 0; i < config.getPatterns().size(); i++) {
          String pattern = config.getPatterns().get(i);
          if (pathMatcher.match(pattern, host)) {
            match = pattern;
            break;
          }
        }

        if (match != null) {
          logger.info("Found match on pattern: " + match + " for host: " + host);
          Map<String, String> variables = pathMatcher.extractUriTemplateVariables(match, host);
          ServerWebExchangeUtils.putUriTemplateVariables(exchange, variables);
          return true;
        }
      }
      return false;
    };
  }

  @Validated
  public static final class Config {

    private List<String> patterns;

    public Config(List<String> patterns) {
      this.patterns = patterns;
    }

    public List<String> getPatterns() {
      return patterns;
    }

    public Config setPatterns(List<String> patterns) {
      this.patterns = patterns;
      return this;
    }
  }
}
