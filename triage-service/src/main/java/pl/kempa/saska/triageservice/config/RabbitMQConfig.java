package pl.kempa.saska.triageservice.config;

import java.util.Map;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	@Value("${spring.rabbitmq.host}")
	private String host;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	@Value("${spring.rabbitmq.exchange.receipt-failed}")
	private String receiptFailedEx;

	@Value("${spring.rabbitmq.queue.receipt-failed}")
	private String receiptFailedQ;

	@Value("${spring.rabbitmq.exchange.receipt-return}")
	private String receiptReturnEx;

	@Value("${spring.rabbitmq.queue.receipt-produce-1}")
	private String receiptProduceQ1;

	@Value("${spring.rabbitmq.queue.receipt-produce-2}")
	private String receiptProduceQ2;

	@Value("${spring.rabbitmq.exchange.dead-letter}")
	private String dlx;

	@Value("${spring.rabbitmq.routing-key.dead-letter}")
	private String dlrk;

	@Value("${spring.rabbitmq.queue.dead-letter}")
	private String dlq;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
		cachingConnectionFactory.setUsername(username);
		cachingConnectionFactory.setPassword(password);
		return cachingConnectionFactory;
	}

	@Bean
	public Declarables receiptFailedBindings() {
		var failedQueue = new Queue(receiptFailedQ, true);
		var fanoutExchange = new FanoutExchange(receiptFailedEx, true, false);
		return new Declarables(
				failedQueue,
				fanoutExchange,
				BindingBuilder.bind(failedQueue).to(fanoutExchange));
	}

	@Bean
	public HeadersExchange receiptReturnExchange() {
		return ExchangeBuilder.headersExchange(receiptReturnEx)
				.durable(true)
				.build();
	}

	@Bean
	public Declarables receiptReturnBindings() {
		var receiptQueue1 = new Queue(receiptProduceQ1, true);
		var receiptQueue2 = new Queue(receiptProduceQ2, true);
		return new Declarables(
				BindingBuilder.bind(receiptQueue1)
						.to(receiptReturnExchange())
						.whereAll(Map.of("queue", receiptProduceQ1))
						.match(),
				BindingBuilder.bind(receiptQueue2)
						.to(receiptReturnExchange())
						.whereAll(Map.of("queue", receiptProduceQ2))
						.match()
		);
	}

	@Bean
	public Declarables deadLetterBinding() {
		var dlQueue = QueueBuilder.durable(dlq)
				.build();
		var dlExchange = new DirectExchange(dlx, true, false);
		return new Declarables(
				dlQueue,
				dlExchange,
				BindingBuilder.bind(dlQueue).to(dlExchange).with(dlrk));
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}
}
