/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.reactiverj4circuitbreakerdemo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.commons.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ryan Baxter
 */
@RestController
public class DemoController {

	Logger LOG = LoggerFactory.getLogger(DemoController.class);

	private ReactiveCircuitBreakerFactory circuitBreakerFactory;
	private HttpBinService httpBin;

	public DemoController(ReactiveCircuitBreakerFactory circuitBreakerFactory, HttpBinService httpBinService) {
		this.circuitBreakerFactory = circuitBreakerFactory;
		this.httpBin = httpBinService;
	}

	@GetMapping("/get")
	public Mono<Map> get() {
		return httpBin.get();
	}

	@GetMapping("/delay/{seconds}")
	public Mono<Map> delay(@PathVariable int seconds) {
		return circuitBreakerFactory.create("delay").run(httpBin.delay(seconds), t -> {
			LOG.warn("delay call failed error", t);
			Map<String, String> fallback = new HashMap<>();
			fallback.put("hello", "world");
			return Mono.just(fallback);
		});
	}

	@GetMapping("/fluxdelay/{seconds}")
	public Flux<String> fluxDelay(@PathVariable int seconds) {
		return circuitBreakerFactory.create("delay").run(httpBin.fluxDelay(seconds), t -> {
			LOG.warn("delay call failed error", t);
			return Flux.just("hello", "world");
		});
	}
}
