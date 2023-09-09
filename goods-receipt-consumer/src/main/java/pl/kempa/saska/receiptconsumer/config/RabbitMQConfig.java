package pl.kempa.saska.receiptconsumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

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

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
		cachingConnectionFactory.setUsername(username);
		cachingConnectionFactory.setPassword(password);
		return cachingConnectionFactory;
	}

	@Bean
	public Declarables fanoutBindings() {
		Queue fanoutQueue1 = new Queue(receiptProduceQ1, true);
		Queue fanoutQueue2 = new Queue(receiptProduceQ2, true);
		FanoutExchange fanoutExchange = new FanoutExchange(receiptProduceEx, true, false);
		return new Declarables(
				fanoutQueue1,
				fanoutQueue2,
				fanoutExchange,
				BindingBuilder.bind(fanoutQueue1).to(fanoutExchange),
				BindingBuilder.bind(fanoutQueue2).to(fanoutExchange));
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
	public MessageRecoverer receiptMessageRecoverer(ConnectionFactory connectionFactory) {
		return new ReceiptErrorMessageResolver(
				rabbitTemplate(connectionFactory),
				jsonMessageConverter()
		);
	}

	@Bean
	public RetryOperationsInterceptor retryInterceptor(ConnectionFactory connectionFactory) {
		return RetryInterceptorBuilder.StatelessRetryInterceptorBuilder
				.stateless()
				.maxAttempts(3)
				.backOffOptions(2000, 1, 100000)
				.recoverer(receiptMessageRecoverer(connectionFactory))
				.build();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory());
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setAdviceChain(retryInterceptor(connectionFactory));
		return factory;
	}
}
