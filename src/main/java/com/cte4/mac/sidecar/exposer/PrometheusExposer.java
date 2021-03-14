package com.cte4.mac.sidecar.exposer;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PrometheusExposer {
	@Value("${exporter.httpserver}")
	private int port;

    @PostConstruct
    public void startService() {
		InetSocketAddress socket = null;
		try {
			log.info(String.format("start sockert servr at %s", this.port));
			socket = new InetSocketAddress(port);
			new HTTPServer(socket, CollectorRegistry.defaultRegistry);
		} catch (Exception e) {
			log.fatal(String.format("fail to bring up prometheus exporter server at {}", this.port));
		}        
		// DUMMY testing
		dummyReg();
    }

	private void dummyReg() {
		TargetMetricCollector tm = new TargetMetricCollector();
		tm.register();
	}

}
