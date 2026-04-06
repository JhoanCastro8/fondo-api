package com.thinkus.fondos.repository.impl;

import com.thinkus.fondos.domain.Client;
import com.thinkus.fondos.repository.ClientRepository;
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
public class ClientRepositoryImpl implements ClientRepository {

    private final DynamoDbClient dynamoDbClient;

    private static final String TABLE_NAME = "clients";

    @Override
    public Optional<Client> findById(String clientId) {
        System.out.println("TABLAS DESDE APP: " + dynamoDbClient.listTables().tableNames());
        Map<String, AttributeValue> key = Map.of(
                "clientId", AttributeValue.builder().s(clientId).build()
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

        Client client = Client.builder()
                .clientId(item.get("clientId").s())
                .name(item.get("name").s())
                .balance(new BigDecimal(item.get("balance").n()))
                .notificationPreference(item.get("notificationPreference").s())
                .build();

        return Optional.of(client);

    }

    @Override
    public void update(Client client) {

        Map<String, AttributeValue> item = Map.of(
                "clientId", AttributeValue.builder().s(client.getClientId()).build(),
                "name", AttributeValue.builder().s(client.getName()).build(),
                "balance", AttributeValue.builder().n(client.getBalance().toString()).build(),
                "notificationPreference", AttributeValue.builder().s(client.getNotificationPreference()).build()
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);

    }

}
