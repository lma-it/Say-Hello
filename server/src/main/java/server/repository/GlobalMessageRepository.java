package server.repository;

import org.repository.repo.Repository;
import server.model.GlobalChat;
import server.model.GlobalMessage;

public interface GlobalMessageRepository extends Repository<GlobalMessage, GlobalChat> {
}
