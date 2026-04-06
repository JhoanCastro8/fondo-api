package com.thinkus.fondos.service.impl;

import com.thinkus.fondos.domain.Client;
import com.thinkus.fondos.domain.Fund;
import com.thinkus.fondos.domain.Transaction;
import com.thinkus.fondos.dto.SubscribeRequestDTO;
import com.thinkus.fondos.dto.SubscriptionResponseDTO;
import com.thinkus.fondos.dto.TransactionResponseDTO;
import com.thinkus.fondos.exception.BusinessException;
import com.thinkus.fondos.repository.ClientRepository;
import com.thinkus.fondos.repository.SubscriptionRepository;
import com.thinkus.fondos.repository.TransactionRepository;
import com.thinkus.fondos.service.SubscriptionService;
import com.thinkus.fondos.util.FundCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final DynamoDbClient dynamoDbClient;

    @Override
    public SubscriptionResponseDTO subscribe(SubscribeRequestDTO request) {

        // Validar cliente
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        // Obtener fondo
        Fund fund = FundCatalog.FUNDS.get(request.getFundId());

        if (fund == null) {
            throw new BusinessException("Fondo no existe");
        }

        // Validar monto mínimo
        if (request.getAmount().compareTo(fund.getMinAmount()) < 0) {
            throw new BusinessException("Monto menor al mínimo requerido");
        }

        // Validar saldo con nombre del fondo
        if (client.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(
                    "No tiene saldo disponible para vincularse al fondo " + fund.getName()
            );
        }

        // Validacion duplicidad
        Optional<?> existing = subscriptionRepository
                .findByClientIdAndFundId(request.getClientId(), request.getFundId());

        if (existing.isPresent()) {
            throw new BusinessException("Ya está suscrito a este fondo");
        }

        BigDecimal amount = request.getAmount();

        // Generar nuevo transactionId
        String transactionId = UUID.randomUUID().toString();

        // Actualizar saldo
        Map<String, AttributeValue> clientKey = Map.of(
                "clientId", AttributeValue.builder().s(request.getClientId()).build()
        );

        Update updateBalance = Update.builder()
                .tableName("clients")
                .key(clientKey)
                .updateExpression("SET balance = balance - :amount")
                .conditionExpression("balance >= :amount")
                .expressionAttributeValues(Map.of(
                        ":amount", AttributeValue.builder().n(amount.toString()).build()
                ))
                .build();

        // Crear suscripción
        Map<String, AttributeValue> subscriptionItem = Map.of(
                "clientId", AttributeValue.builder().s(request.getClientId()).build(),
                "fundId", AttributeValue.builder().s(request.getFundId()).build(),
                "amount", AttributeValue.builder().n(amount.toString()).build(),
                "status", AttributeValue.builder().s("ACTIVE").build()
        );

        Put putSubscription = Put.builder()
                .tableName("subscriptions")
                .item(subscriptionItem)
                .conditionExpression("attribute_not_exists(fundId)")
                .build();

        // Crear transacción del fondo
        Map<String, AttributeValue> transactionItem = Map.of(
                "transactionId", AttributeValue.builder().s(transactionId).build(),
                "clientId", AttributeValue.builder().s(request.getClientId()).build(),
                "timestamp", AttributeValue.builder().s(Instant.now().toString()).build(),
                "fundId", AttributeValue.builder().s(request.getFundId()).build(),
                "fundName", AttributeValue.builder().s(fund.getName()).build(), // 👈 NUEVO
                "type", AttributeValue.builder().s("OPEN").build(),
                "amount", AttributeValue.builder().n(amount.toString()).build()
        );

        Put putTransaction = Put.builder()
                .tableName("transactions")
                .item(transactionItem)
                .build();

        // Ejecutar transacción
        TransactWriteItemsRequest transactRequest = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder().update(updateBalance).build(),
                        TransactWriteItem.builder().put(putSubscription).build(),
                        TransactWriteItem.builder().put(putTransaction).build()
                )
                .build();

        dynamoDbClient.transactWriteItems(transactRequest);

        return SubscriptionResponseDTO.builder()
                .message("Suscripción exitosa")
                .status("OK")
                .build();

    }

    @Override
    public void cancel(String clientId, String fundId) {

        var subscriptionOpt = subscriptionRepository
                .findByClientIdAndFundId(clientId, fundId);

        if (subscriptionOpt.isEmpty()) {
            throw new BusinessException("No existe suscripción");
        }

        var subscription = subscriptionOpt.get();

        if (!subscription.getStatus().equals("ACTIVE")) {
            throw new BusinessException("La suscripción ya está cancelada");
        }

        BigDecimal amount = subscription.getAmount();

        // Obtener fondo
        Fund fund = FundCatalog.FUNDS.get(fundId);

        // Generar transactionId
        String transactionId = UUID.randomUUID().toString();

        // Devolver saldo
        Map<String, AttributeValue> clientKey = Map.of(
                "clientId", AttributeValue.builder().s(clientId).build()
        );

        Update updateBalance = Update.builder()
                .tableName("clients")
                .key(clientKey)
                .updateExpression("SET balance = balance + :amount")
                .expressionAttributeValues(Map.of(
                        ":amount", AttributeValue.builder().n(amount.toString()).build()
                ))
                .build();

        // Cambiar estado
        Map<String, AttributeValue> subKey = Map.of(
                "clientId", AttributeValue.builder().s(clientId).build(),
                "fundId", AttributeValue.builder().s(fundId).build()
        );

        Update updateSubscription = Update.builder()
                .tableName("subscriptions")
                .key(subKey)
                .updateExpression("SET #status = :status")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(Map.of(
                        ":status", AttributeValue.builder().s("CANCELLED").build()
                ))
                .build();

        Map<String, AttributeValue> transactionItem = Map.of(
                "transactionId", AttributeValue.builder().s(transactionId).build(),
                "clientId", AttributeValue.builder().s(clientId).build(),
                "timestamp", AttributeValue.builder().s(Instant.now().toString()).build(),
                "fundId", AttributeValue.builder().s(fundId).build(),
                "fundName", AttributeValue.builder().s(fund.getName()).build(), // 👈 NUEVO
                "type", AttributeValue.builder().s("CANCEL").build(),
                "amount", AttributeValue.builder().n(amount.toString()).build()
        );

        Put putTransaction = Put.builder()
                .tableName("transactions")
                .item(transactionItem)
                .build();

        TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder().update(updateBalance).build(),
                        TransactWriteItem.builder().update(updateSubscription).build(),
                        TransactWriteItem.builder().put(putTransaction).build()
                )
                .build();

        dynamoDbClient.transactWriteItems(request);
    }

    @Override
    public List<TransactionResponseDTO> getTransactions(String clientId) {

        List<Transaction> transactions = transactionRepository.findByClientId(clientId);

        return transactions.stream()
                .map(t -> TransactionResponseDTO.builder()
                        .fundId(t.getFundId())
                        .type(t.getType())
                        .amount(t.getAmount())
                        .timestamp(t.getTimestamp())
                        .build()
                )
                .toList();
    }

}
