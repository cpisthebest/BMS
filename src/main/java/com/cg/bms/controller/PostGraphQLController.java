package com.cg.bms.controller;

import com.cg.bms.model.Post;
import com.cg.bms.model.PostData;
import com.cg.bms.model.PostInput;
import com.cg.bms.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class PostGraphQLController {
    private final PostService postService;
    @Autowired
    public PostGraphQLController(PostService postService){
        this.postService=postService;
    }

    @SneakyThrows
    @QueryMapping("getPosts")
    Mono<PostData> getPosts(@Min(0) @Argument int pageNumber,
                            @Min(1) @Max(100) @Argument int pageSize){
        return this.postService.getPosts(PageRequest.of(pageNumber, pageSize, Sort.by("id")));
    }
    @QueryMapping("getAllPosts")
    public Flux<Post> getAllPosts(){
        log.info("Get all posts using 'getAllPosts' query");
        return this.postService.getAllPosts();
    }
    @MutationMapping("addPost")
    @PreAuthorize("(hasAuthority('SCOPE_profile'))")
    public Mono<Post> addPost(@Valid @Argument PostInput postInput){
        log.info("Add post - input - "+postInput);
        return this.postService.addPost(postInput);
    }
    @QueryMapping("getPostById")
    public Mono<Post> getPostById(@NotNull(message="{id.not.null}") @Argument Long id){
        log.info("Inside getPostById - id - "+ id);
        return this.postService.getPostById(id);
    }
    @MutationMapping("updatePost")
    @PreAuthorize("(hasAuthority('SCOPE_profile'))")
    public Mono<Post> updatePost(@NotNull(message="{id.not.null}") @Argument Long id,@Valid @Argument PostInput postInput){
        log.info("Updating post for id - {} with input - {}",id,postInput);
        return this.postService.updatePost(id,postInput);
    }
    @MutationMapping("deletePost")
    @PreAuthorize("(hasAuthority('SCOPE_profile'))")
    public Mono<Post> deletePost(@NotNull(message="{id.not.null}") @Argument Long id){
        log.info("Deleting post with id - "+id);
        return this.postService.deletePost(id);
    }
}
