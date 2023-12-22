package com.bjoggis.jonasandersengateway;

import com.bjoggis.jonasandersengateway.SubdomainRoutePredicateFactory.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JonasandersenGatewayApplication {


  public static void main(String[] args) {
    SpringApplication.run(JonasandersenGatewayApplication.class, args);
  }

  @Bean
  SubdomainRoutePredicateFactory myHostRoutePredicateFactory() {
    return new SubdomainRoutePredicateFactory();
  }

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
      SubdomainRoutePredicateFactory subdomainRoutePredicateFactory,
      GatewayProperties properties) {

    final Builder routes = builder.routes();
    for (GatewayProperties.RouteInfo routeInfo : properties.routes().values()) {
      routes.route(r -> r.predicate(subdomainRoutePredicateFactory.apply(new Config(
              routeInfo.patterns())))
          .filters(f -> f.setHostHeader(routeInfo.hostHeader()))
          .uri("http://kourier-internal.knative-serving.svc.cluster.local:80")
      );
    }
    return routes.build();
  }
}
