package server.repository;

import server.model.GlobalChat;
import org.repository.repo.Repository;

import java.util.List;

public interface GlobalChatRepository extends Repository<GlobalChat, Long> {
    List<GlobalChat> getGlobalChat();
}
