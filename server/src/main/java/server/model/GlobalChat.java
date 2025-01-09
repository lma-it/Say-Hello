package server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "global_chat")
public class GlobalChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalId;

    @Column(name = "clientId")
    private Long clientId;

    @Column(name = "global_messages")
    private List<String> messages;

    public GlobalChat(){}

    public GlobalChat(Long id, List<String> messages){
        this.clientId = id;
        this.messages = messages;
    }

}
