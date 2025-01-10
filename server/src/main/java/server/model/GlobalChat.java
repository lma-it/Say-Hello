package server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

// Подумать над структурой, потому что в данном случае у каждого пользователя свой глобальный чат
// и он получает из базы данных только те сообщения, которые сам отправлял.
@Getter
@Setter
@Entity
@Table(name = "global_chat")
public class GlobalChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalId;

    @ManyToOne
    @JoinColumn(name = "clientId")
    private Client client;

    @OneToMany(mappedBy = "globalChat")
    private List<GlobalMessage> messages;

    public GlobalChat() {}

    public GlobalChat(Client client, List<GlobalMessage> messages) {
        this.client = client;
        this.messages = messages;
    }

    @Override
    public String toString() {
        return String.format("Id: %s, Список сообщений: %s", this.globalId, this.messages);
    }
}

