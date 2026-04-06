package com.thinkus.fondos.repository.impl;

import com.thinkus.fondos.domain.Subscription;
import com.thinkus.fondos.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final DynamoDbClient dynamoDbClient;

    private static final String TABLE_NAME = "subscriptions";

    @Override
    public void save(Subscription subscription) {

        Map<String, AttributeValue> item = Map.of(
                "clientId", AttributeValue.builder().s(subscription.getClientId()).build(),
                "fundId", AttributeValue.builder().s(subscription.getFundId()).build(),
                "amount", AttributeValue.builder().n(subscription.getAmount().toString()).build(),
                "status", AttributeValue.builder().s(subscription.getStatus()).build()
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    @Override
    public Optional<Subscription> findByClientIdAndFundId(String clientId, String fundId) {

        Map<String, AttributeValue> key = Map.of(
                "clientId", AttributeValue.builder().s(clientId).build(),
                "fundId", AttributeValue.builder().s(fundId).build()
        );

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (response.item().isEmpty()) {
            return Optional.empty();
        }

        Map<String, AttributeValue> item = response.item();

        Subscription subscription = Subscription.builder()
                .clientId(item.get("clientId").s())
                .fundId(item.get("fundId").s())
                .amount(new BigDecimal(item.get("amount").n()))
                .status(item.get("status").s())
                .build();

        return Optional.of(subscription);
    }

}
