package server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "private_messages")
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long privateMessageId;

    @ManyToOne
    @JoinColumn(name = "private_chat_id")
    private PrivateChat privateChat;

    @Column(name = "message")
    private String message;

    public PrivateMessage() {}

    public PrivateMessage(PrivateChat privateChat, String message) {
        this.privateChat = privateChat;
        this.message = message;
    }
}

