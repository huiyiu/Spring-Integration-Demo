/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyu.si.config;

import java.util.List;

import com.hyu.si.model.Delivery;
import com.hyu.si.model.Drink;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;

/**
 * @author Marius Bogoevici
 */
@MessageEndpoint
public class OrderAggregator {

	@Aggregator(inputChannel = "preparedDrinks", outputChannel = "deliveries")
	public Delivery prepareDelivery(List<Drink> drinks) {
		return new Delivery(drinks);
	}

	@CorrelationStrategy
	public int correlateByOrderNumber(Drink drink) {
		return drink.getOrderNumber();
	}

}
