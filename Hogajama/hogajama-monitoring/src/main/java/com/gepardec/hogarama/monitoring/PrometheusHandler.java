package com.gepardec.hogarama.monitoring;

import com.gepardec.hogarama.domain.metrics.Metrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Path("/metrics")
//@GZIP
@ApplicationScoped
public class PrometheusHandler {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusHandler.class);

    private CollectorRegistry prometheusRegistry;


    @PostConstruct
    public void init(){

        prometheusRegistry = CollectorRegistry.defaultRegistry;
        new HogaramaExports().register();
        Metrics.requestsTotal.labels("hogarama_monitoring", "init").inc();
        DefaultExports.initialize();
    }

    @GET
    @Produces(TextFormat.CONTENT_TYPE_004)
    public Response getMetrics() throws IOException{
        Summary.Timer requestTimer = Metrics.requestLatency.labels("hogarama_monitoring", "get_metrics").startTimer();
        Metrics.requestsTotal.labels("hogarama_monitoring", "get_metrics").inc();
        HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, prometheusRegistry.filteredMetricFamilySamples(parse(request)));
        logger.info("GET Request {}", request);
        writer.flush();
        requestTimer.observeDuration();
        return Response.ok(writer.toString()).build();
    }

    private Set<String> parse(HttpServletRequest req) {
        String[] includedParam = req.getParameterValues("name[]");
        if (includedParam == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<>(Arrays.asList(includedParam));
        }
    }
}