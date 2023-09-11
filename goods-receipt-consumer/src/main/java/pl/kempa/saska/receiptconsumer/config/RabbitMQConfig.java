package pl.kempa.saska.receiptconsumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
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

	@Value("${spring.rabbitmq.exchange.receipt-produce}")
	private String receiptProduceEx;

	@Value("${spring.rabbitmq.queue.receipt-produce-1}")
	private String receiptProduceQ1;

	@Value("${spring.rabbitmq.queue.receipt-produce-2}")
	private String receiptProduceQ2;

	@Value("${spring.rabbitmq.exchange.dead-letter}")
	private String dlx;

	@Value("${spring.rabbitmq.routing-key.dead-letter}")
	private String dlrk;

	@Value("${rabbitmq.message-ttl}")
	private int messageTTL;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
		cachingConnectionFactory.setUsername(username);
		cachingConnectionFactory.setPassword(password);
		return cachingConnectionFactory;
	}

	@Bean
	public Declarables mainQueuesBindings() {
		var receiptQueue1 = QueueBuilder.durable(receiptProduceQ1)
				.deadLetterExchange(dlx)
				.deadLetterRoutingKey(dlrk)
				.ttl(messageTTL)
				.maxLength(1)
				.build();
		var receiptQueue2 = QueueBuilder.durable(receiptProduceQ2)
				.deadLetterExchange(dlx)
				.deadLetterRoutingKey(dlrk)
				.ttl(messageTTL)
				.maxLength(1)
				.build();
		var topicExchange = new TopicExchange(receiptProduceEx, true, false);
		return new Declarables(
				receiptQueue1,
				receiptQueue2,
				topicExchange,
				BindingBuilder.bind(receiptQueue1).to(topicExchange).with("rk.goods-receipt-produce-1"),
				BindingBuilder.bind(receiptQueue2).to(topicExchange).with("rk.goods-receipt-produce-2"));
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

	@Bean
	public ReceiptErrorMessageResolver receiptMessageRecoverer(ConnectionFactory connectionFactory) {
		return new ReceiptErrorMessageResolver(
				rabbitTemplate(connectionFactory),
				jsonMessageConverter()
		);
	}

	@Bean
	public RetryQueuesInterceptor retryQueuesInterceptor(ConnectionFactory connectionFactory) {
		var interceptor = new RetryQueuesInterceptor();
		interceptor.setRabbitTemplate(rabbitTemplate(connectionFactory));
		interceptor.setRecoverer(receiptMessageRecoverer(connectionFactory));
		return interceptor;
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory());
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setAdviceChain(retryQueuesInterceptor(connectionFactory));
		return factory;
	}
}
