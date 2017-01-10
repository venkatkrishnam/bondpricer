package com.spectragrp.bondpricer;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.google.gson.Gson;

public class PricingEngine {
	public static void main(String[] args) {
		String groupId = "bondpricer";
		String subscribeTopic = "bond";
		String publishTopic = "pricedBond";
		ExecutorService executor = Executors.newSingleThreadExecutor();

		Pricer pricer = new Pricer();
		ConsumerLoop consumer = new ConsumerLoop(groupId, subscribeTopic, publishTopic, pricer);
		executor.submit(consumer);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				consumer.shutdown();
				executor.shutdown();
				try {
					executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static class ConsumerLoop implements Runnable {
		private final KafkaConsumer<String, String> consumer;
		private final KafkaProducer<Integer, String> producer;
		private final String subscribeTopic;
		private final String publishTopic;
		private Pricer pricer;
		private Gson gson;

		public ConsumerLoop(String groupId, String subscribeTopic, String publishTopic, Pricer pricer) {
			this.subscribeTopic = subscribeTopic;
			this.publishTopic = publishTopic;
			this.pricer = pricer;
			Properties props = new Properties();
			props.put("bootstrap.servers", "localhost:9092");
			props.put("group.id", groupId);
			props.put("key.deserializer", StringDeserializer.class.getName());
			props.put("value.deserializer", StringDeserializer.class.getName());
			props.put("key.serializer", StringSerializer.class.getName());
			props.put("value.serializer", StringSerializer.class.getName());
			this.consumer = new KafkaConsumer<>(props);
			this.producer = new KafkaProducer<>(props);
			gson = new Gson();
		}

		@Override
		public void run() {
			try {
				consumer.subscribe(Arrays.asList(subscribeTopic));

				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(10000);
					for (ConsumerRecord<String, String> record : records) {
						String bondJson = record.value();
						Bond bond = gson.fromJson(bondJson, Bond.class);
						
						//price the bond
						double price = 
							pricer.price(bond.issueDate.toEpochDay(), bond.maturityDate.toEpochDay(), bond.coupon, bond.parValue, bond.paymentsPerYear);
						PricedBond pricedBond = new PricedBond(bond, price);
						
						//public the price
						String pricedBondJson = gson.toJson(pricedBond);
						producer.send(new ProducerRecord<Integer, String>(publishTopic, pricedBondJson));
					}
				}
			} catch (WakeupException e) {
				// ignore for shutdown
			} finally {
				consumer.close();
			}
		}

		public void shutdown() {
			consumer.wakeup();
		}
	}
}
