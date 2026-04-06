package com.thinkus.fondos.repository;

import com.thinkus.fondos.domain.Client;

import java.util.Optional;

public interface ClientRepository {

    Optional<Client> findById(String clientId);
    void update(Client client);

}
