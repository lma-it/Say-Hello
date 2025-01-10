package server.repository;

import server.model.Client;
import server.model.GlobalChat;
import org.repository.repo.Repository;

public interface GlobalChatRepository extends Repository<GlobalChat, Long> {
    GlobalChat getChatByClient(Client client);
}
