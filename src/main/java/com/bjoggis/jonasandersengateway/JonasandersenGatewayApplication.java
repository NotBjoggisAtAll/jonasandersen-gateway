package com.bjoggis.jonasandersengateway;

import com.bjoggis.jonasandersengateway.SubdomainRoutePredicateFactory.Config;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

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
      PathRoutePredicateFactory pathRoutePredicateFactory,
      GatewayProperties properties) {

    final Builder routes = builder.routes();
    for (GatewayProperties.RouteInfo routeInfo : properties.routes().values()) {
      String pathPrefix = routeInfo.pathPrefix();
      if (StringUtils.hasText(pathPrefix)) {
        routes.route(r -> r
            .predicate(subdomainRoutePredicateFactory.apply(new Config(routeInfo.patterns()))
                .and(pathRoutePredicateFactory.apply(
                    c -> c.setPatterns(List.of("/%s/**".formatted(pathPrefix))))))
            .filters(f -> f.setHostHeader(routeInfo.hostHeader())
                .rewritePath("/%s/(?<segment>.*)".formatted(pathPrefix), "/${segment}"))
            .uri("http://kourier-internal.knative-serving.svc.cluster.local:80")
        );
      } else {
        routes.route(r -> r.predicate(subdomainRoutePredicateFactory.apply(new Config(
                routeInfo.patterns())))
            .filters(f -> f.setHostHeader(routeInfo.hostHeader()))
            .uri("http://kourier-internal.knative-serving.svc.cluster.local:80")
        );
      }
    }
    return routes.build();
  }
}
