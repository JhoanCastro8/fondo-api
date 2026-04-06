package com.thinkus.fondos.repository.impl;

import com.thinkus.fondos.domain.Transaction;
import com.thinkus.fondos.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final DynamoDbClient dynamoDbClient;

    private static final String TABLE_NAME = "transactions";

    @Override
    public List<Transaction> findByClientId(String clientId) {

        Map<String, AttributeValue> expressionValues = Map.of(
                ":clientId", AttributeValue.builder().s(clientId).build()
        );

        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream().map(item ->
                Transaction.builder()
                        .clientId(item.get("clientId").s())
                        .timestamp(item.get("timestamp").s())
                        .fundId(item.get("fundId").s())
                        .type(item.get("type").s())
                        .amount(new BigDecimal(item.get("amount").n()))
                        .build()
        ).toList();
    }

    @Override
    public void save(Transaction transaction) {

        Map<String, AttributeValue> item = Map.of(
                "clientId", AttributeValue.builder().s(transaction.getClientId()).build(),
                "timestamp", AttributeValue.builder().s(transaction.getTimestamp()).build(),
                "fundId", AttributeValue.builder().s(transaction.getFundId()).build(),
                "type", AttributeValue.builder().s(transaction.getType()).build(),
                "amount", AttributeValue.builder().n(transaction.getAmount().toString()).build()
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
}
