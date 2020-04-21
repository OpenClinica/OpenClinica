package com.openclinica.kafka;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

  private static final String KAFKA_BROKER_ADDRESS = CoreResources.getKafkaBrokers();;
  public static final String STUDY_PUBLISH_TOPIC = "studyPublish";
  public static final String EVENT_ATTRIBUTE_CHANGE_TOPIC = "eventAttributeChange";
  public static final String FORM_CHANGE_TOPIC = "formChange";
  public static final String ITEM_DATA_CHANGE_TOPIC = "itemDataChange";

  @Bean
  public ProducerFactory<Integer, String> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER_ADDRESS);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public KafkaTemplate<Integer, String> kafkaTemplate() {
    return new KafkaTemplate<Integer, String>(producerFactory());
  }

  @Bean
  public KafkaAdmin admin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER_ADDRESS);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic studyPublishEvent() {
    return new NewTopic(STUDY_PUBLISH_TOPIC, 1, (short) 1);
  }

  @Bean
  public NewTopic eventAttributeChange() {
    return new NewTopic(EVENT_ATTRIBUTE_CHANGE_TOPIC, 1, (short) 1);
  }

  @Bean
  public NewTopic formClosure() {
    return new NewTopic(FORM_CHANGE_TOPIC, 3, (short) 1);
  }

  @Bean
  public NewTopic itemDataChange() {
    return new NewTopic(ITEM_DATA_CHANGE_TOPIC, 1, (short) 1);
  }

}
