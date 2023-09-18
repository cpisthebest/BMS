package com.cg.bms.service;

import com.cg.bms.model.Post;
import com.cg.bms.model.PostData;
import com.cg.bms.model.PostInput;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

public interface PostService {
    Mono<PostData> getPosts(PageRequest pageRequest) throws ExecutionException, InterruptedException;
    Flux<Post> getAllPosts();
    Mono<Post> getPostById(Long id);
    Mono<Post> addPost(PostInput postInput);
    Mono<Post> updatePost(Long id,PostInput postInput);
    Mono<Post> deletePost(Long id);
}
