package server.repository;

import org.repository.repo.Repository;
import server.model.GlobalChat;
import server.model.GlobalMessage;

import java.util.List;

public interface GlobalMessageRepository extends Repository<GlobalMessage, GlobalChat> {
    List<GlobalMessage> getAllMessages();
}
