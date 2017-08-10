package org.chiwooplatform.samples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootConfiguration
// @ImportAutoConfiguration(classes = { MongoAutoConfiguration.class,
// MongoDataAutoConfiguration.class, MongoRepositoriesAutoConfiguration.class })
public class AbstractDynamoTests {
	@Bean
	public ObjectMapper objectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.indentOutput(true);
		builder.failOnUnknownProperties(false);
		return builder.build();
	}

	@EnableDynamoDBRepositories(basePackages = "org.chiwooplatform.samples.dam.dynamo")
	@Configuration
	public class DynamoConfiguration {

		@Value("${amazon.dynamodb.endpoint}")
		private String amazonDynamoDBEndpoint;

		@Value("${amazon.aws.accesskey:accessKey}")
		private String amazonAWSAccessKey;

		@Value("${amazon.aws.secretkey:secretkey}")
		private String amazonAWSSecretKey;

		@Value("${amazon.aws.region:secretkey}")
		private String amazonAWSRegion;

		@Bean
		public AmazonDynamoDB amazonLocalDynamoDB() {
			AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
			builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials()));
			builder.setEndpointConfiguration(
					new EndpointConfiguration(amazonDynamoDBEndpoint, amazonAWSRegion));
			final AmazonDynamoDB amazonDynamoDB = builder.build();
			return amazonDynamoDB;
		}

		@Bean
		public AWSCredentials awsCredentials() {
			return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
		}

		@Bean
		public AmazonDynamoDB amazonDynamoDB() {
			AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
			AWSCredentials awsCredentials = awsCredentials();
			if (awsCredentials != null) {
				builder.withCredentials(
						new AWSStaticCredentialsProvider(awsCredentials()));
			}
			if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
				builder.setEndpointConfiguration(new EndpointConfiguration(
						amazonDynamoDBEndpoint, amazonAWSRegion));
			}
			final AmazonDynamoDB amazonDynamoDB = builder.build();
			return amazonDynamoDB;
		}

		@Bean
		public DynamoDBMapper dynamoDBMapper() {
			final DynamoDBMapperConfig dbMapperConfig = DynamoDBMapperConfig.builder()
					.withTableNameOverride(TableNameOverride.withTableNamePrefix(""))
					.build();
			final DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB(),
					dbMapperConfig);
			return mapper;
		}

	}
}