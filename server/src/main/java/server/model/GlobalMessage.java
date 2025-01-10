package server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "global_messages")
public class GlobalMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalMessageId;

    @ManyToOne
    @JoinColumn(name = "global_chat_id")
    private GlobalChat globalChat;

    @Column(name = "message")
    private String message;

    public GlobalMessage() {}

    public GlobalMessage(GlobalChat globalChat, String message) {
        this.globalChat = globalChat;
        this.message = message;
    }

    @Override
    public String toString() {
        return "message: " + message;
    }
}

