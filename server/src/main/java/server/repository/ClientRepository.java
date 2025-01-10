package server.repository;

import org.repository.repo.Repository;
import server.model.Client;

public interface ClientRepository extends Repository<Client, Long> {
    Client getEntityByLoginAndPassword(String login, String password);
}
