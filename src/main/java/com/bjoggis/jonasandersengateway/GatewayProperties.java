package com.bjoggis.jonasandersengateway;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jonas.gateway")
public record GatewayProperties(Map<String, RouteInfo> routes) {

  public record RouteInfo(List<String> patterns, String hostHeader, String pathPrefix) {

  }
}
