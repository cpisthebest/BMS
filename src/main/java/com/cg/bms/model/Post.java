package com.cg.bms.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Post {
    @Id
    private Long id;
    private String title;
    private String content;
    private String author;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    public Post(PostInput postInput)
    {
        this.title= Objects.requireNonNull(postInput.getTitle());
        this.content=postInput.getContent();
        this.author=Objects.requireNonNull(postInput.getAuthor());
        this.createdAt = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }
}
