package pl.kempa.saska.receiptconsumer.config;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.rabbitmq.client.Channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@NoArgsConstructor
@Data
@Slf4j
public class RetryQueuesInterceptor implements MethodInterceptor {

	private RabbitTemplate rabbitTemplate;

	private ReceiptErrorMessageResolver recoverer;

	@Value("${rabbitmq.retry.count}")
	private int countLimit;

	@Value("${spring.rabbitmq.queue.receipt-produce-1}")
	private String receiptProduceQ1;

	@Value("${spring.rabbitmq.queue.receipt-produce-2}")
	private String receiptProduceQ2;

	@Value("${spring.rabbitmq.exchange.receipt-retry-1}")
	private String receiptRetryEx1;

	@Value("${spring.rabbitmq.exchange.receipt-retry-2}")
	private String receiptRetryEx2;

	@Override
	public Object invoke(MethodInvocation invocation)
			throws Throwable {
		return tryConsume(invocation, this::ack, (mac, e) -> {
			try {
				int retryCount = getRetryCount(mac);
				if (retryCount >= countLimit) {
					if (recoverer != null) {
						recoverer.recover(mac.message, e);
					}
					MessageProperties props = mac.message.getMessageProperties();
					mac.channel.basicReject(props.getDeliveryTag(), false);
					return;
				}
				sendToRetryQueue(mac, retryCount);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});
	}

	private Object tryConsume(MethodInvocation invocation, Consumer<MessageAndChannel> successHandler,
	                          BiConsumer<MessageAndChannel, Throwable> errorHandler) {
		var mac =
				new MessageAndChannel((Message) invocation.getArguments()[1], (Channel) invocation.getArguments()[0]);
		Object ret = null;
		try {
			// here listener is called
			ret = invocation.proceed();
			successHandler.accept(mac);
		} catch (Throwable e) {
			errorHandler.accept(mac, e);
		}
		return ret;
	}

	private void ack(MessageAndChannel mac) {
		try {
			var tag = mac.message.getMessageProperties().getDeliveryTag();
			mac.channel.basicAck(tag, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int getRetryCount(MessageAndChannel mac) {
		MessageProperties props = mac.message.getMessageProperties();
		return ofNullable(props.getHeader("x-retried-count"))
				.map(String::valueOf)
				.map(Integer::valueOf)
				.orElse(0);
	}

	private void sendToRetryQueue(MessageAndChannel mac, int retryCount)
			throws Exception {
		var properties = mac.message.getMessageProperties();
		var originalQueues2RetryExchanges = Map.of(receiptProduceQ1, receiptRetryEx1,
				receiptProduceQ2, receiptRetryEx2);
		var retryExchange = originalQueues2RetryExchanges.get(properties.getConsumerQueue());
		rabbitTemplate.convertAndSend(retryExchange, "", mac.message, m -> {
			var props = m.getMessageProperties();
			props.setHeader("x-retried-count", String.valueOf(retryCount + 1));
			props.setHeader("x-original-exchange", props.getReceivedExchange());
			props.setHeader("x-original-routing-key", props.getReceivedRoutingKey());
			return m;
		});
		mac.channel.basicReject(mac.message.getMessageProperties().getDeliveryTag(), false);
	}

	@Data
	@AllArgsConstructor
	private class MessageAndChannel {
		private Message message;
		private Channel channel;
	}
}
