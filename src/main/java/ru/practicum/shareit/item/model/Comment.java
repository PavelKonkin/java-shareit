package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NamedEntityGraph(name = "comment.item.owner", attributeNodes = {
        @NamedAttributeNode("item"),
        @NamedAttributeNode(value = "item", subgraph = "item.user"),
        @NamedAttributeNode("author")
}, subgraphs = @NamedSubgraph(name = "item.user", attributeNodes = @NamedAttributeNode("owner")))
@Table(name = "comments")
@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;
    private String text;
    private LocalDateTime created = LocalDateTime.now();
}
