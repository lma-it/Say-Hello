package server.repository;

import server.model.Client;
import server.model.PrivateChat;
import org.repository.repo.Repository;

public interface PrivateChatRepository extends Repository<PrivateChat, Long> {
    PrivateChat getChatByClient(Client client);
}
