package pl.kempa.saska.receiptconsumer.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryQueuesConfig {

	@Value("${spring.rabbitmq.exchange.receipt-retry-1}")
	private String receiptRetryEx1;

	@Value("${spring.rabbitmq.exchange.receipt-retry-2}")
	private String receiptRetryEx2;

	@Value("${spring.rabbitmq.queue.receipt-retry-1}")
	private String receiptRetryQ1;

	@Value("${spring.rabbitmq.queue.receipt-retry-2}")
	private String receiptRetryQ2;

	@Value("${spring.rabbitmq.exchange.receipt-produce}")
	private String receiptProduceEx;

	@Value("${spring.rabbitmq.routing-key.receipt-produce-1}")
	private String receiptProduceRK1;

	@Value("${spring.rabbitmq.routing-key.receipt-produce-2}")
	private String receiptProduceRK2;

	@Value("${rabbitmq.retry.retry-queue-ttl}")
	private int ttl;

	@Bean
	public Declarables retryBindings() {
		var retryQ1 = QueueBuilder.durable(receiptRetryQ1)
				.deadLetterExchange(receiptProduceEx)
				.deadLetterRoutingKey(receiptProduceRK1)
				.ttl(ttl)
				.build();
		var retryQ2 = QueueBuilder.durable(receiptRetryQ2)
				.deadLetterExchange(receiptProduceEx)
				.deadLetterRoutingKey(receiptProduceRK2)
				.ttl(ttl)
				.build();
		var retryExchange1 = new FanoutExchange(receiptRetryEx1, true, false);
		var retryExchange2 = new FanoutExchange(receiptRetryEx2, true, false);
		return new Declarables(
				retryQ1,
				retryQ2,
				retryExchange1,
				retryExchange2,
				BindingBuilder.bind(retryQ1).to(retryExchange1),
				BindingBuilder.bind(retryQ2).to(retryExchange2));
	}
}
