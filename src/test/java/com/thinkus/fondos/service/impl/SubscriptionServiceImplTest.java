package com.thinkus.fondos.service.impl;

import com.thinkus.fondos.domain.Client;
import com.thinkus.fondos.domain.Subscription;
import com.thinkus.fondos.dto.SubscribeRequestDTO;
import com.thinkus.fondos.exception.BusinessException;
import com.thinkus.fondos.repository.ClientRepository;
import com.thinkus.fondos.repository.SubscriptionRepository;
import com.thinkus.fondos.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceImplTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private SubscriptionServiceImpl service;

    private SubscribeRequestDTO request;

    @BeforeEach
    void setup() {
        request = new SubscribeRequestDTO();
        request.setClientId("1");
        request.setFundId("1");
        request.setAmount(new BigDecimal("75000"));
    }

    @Test
    void shouldSubscribeSuccessfully() {

        Client client = Client.builder()
                .clientId("1")
                .balance(new BigDecimal("500000"))
                .build();

        when(clientRepository.findById("1"))
                .thenReturn(Optional.of(client));

        when(subscriptionRepository.findByClientIdAndFundId("1", "1"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.subscribe(request));

        verify(dynamoDbClient).transactWriteItems((TransactWriteItemsRequest) any());
    }

    @Test
    void shouldFailWhenInsufficientBalance() {

        Client client = Client.builder()
                .clientId("1")
                .balance(new BigDecimal("10000"))
                .build();

        when(clientRepository.findById("1"))
                .thenReturn(Optional.of(client));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.subscribe(request)
        );

        assertTrue(exception.getMessage().contains("No tiene saldo"));
    }

    @Test
    void shouldFailWhenAlreadySubscribed() {

        Client client = Client.builder()
                .clientId("1")
                .balance(new BigDecimal("500000"))
                .build();

        when(clientRepository.findById("1"))
                .thenReturn(Optional.of(client));

        when(subscriptionRepository.findByClientIdAndFundId("1", "1"))
                .thenReturn(Optional.of(new Subscription()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.subscribe(request)
        );

        assertEquals("Ya está suscrito a este fondo", exception.getMessage());
    }
}
