package com.company.salonbooking.shared.application.port;

import com.company.salonbooking.shared.domain.event.DomainEvent;

/**
 * A ÚNICA maneira de casos de uso registrarem eventos de domínio. As implementações DEVEM gravar na
 * tabela de outbox utilizando a transação/conexão existente do chamador — nunca abrindo
 * uma nova e nunca publicando diretamente em um broker neste ponto (Seções 25, 26): o
 * evento torna-se persistente no exato mesmo commit da alteração de negócio que ele descreve.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}