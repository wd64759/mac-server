package com.cte4.mac.server.exposer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PrometheusExposer {
	@Value("${exporter.httpserver}")
	private int port;

	private CollectorRegistry registry = CollectorRegistry.defaultRegistry;

	@PostConstruct
	public void startService() {
		InetSocketAddress socket = null;
		try {
			log.info(String.format("start sockert servr at %s", this.port));
			socket = new InetSocketAddress(port);
			new HTTPServer(socket, registry);
		} catch (Exception e) {
			log.fatal(String.format("fail to bring up prometheus exporter server at {}", this.port));
		}
		new DefaultMonitor().register(registry);
	}

	public CollectorRegistry getRegistry() {
		return this.registry;
	}

	class DefaultMonitor extends Collector {

		long lastCall;

		@Override
		public List<MetricFamilySamples> collect() {
			if (lastCall == 0) {
				lastCall = System.currentTimeMillis();
				log.info("::prometheus exposer::default monitor::scan");
			} else {
				if(lastCall > 0) {
					log.info("::prometheus exposer::default monitor::scan span - " + (int)(System.currentTimeMillis() - lastCall)/1000);
					lastCall = -1;
				}
			}
			return new ArrayList<>();
		}

	}

}
