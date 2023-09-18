package com.cg.bms.service;

import com.cg.bms.exception.DuplicatePostException;
import com.cg.bms.exception.PostNotFoundException;
import com.cg.bms.model.Post;
import com.cg.bms.model.PostData;
import com.cg.bms.model.PostInput;
import com.cg.bms.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository){
        this.postRepository=postRepository;
    }

    @Override
    public Mono<PostData> getPosts(PageRequest pageRequest) throws ExecutionException, InterruptedException {
        Flux<Post> postData = postRepository.findAllBy(pageRequest);
        Long totalCount = postRepository.count().toFuture().get();
        List<Post> postList = new ArrayList<>();
        postData.subscribe(postList::add);
    return
        Mono.just(new PostData(
            postList,
            pageRequest.getPageSize(),
            totalCount, (totalCount%pageRequest.getPageSize()==0)?(totalCount/pageRequest.getPageSize()):(totalCount / pageRequest.getPageSize()) + 1,
            pageRequest.getPageNumber()));
    }

    @Override
    public Flux<Post> getAllPosts() {
        log.info("Fetching all posts");
        return this.postRepository.findAll();
    }

    @Override
    public Mono<Post> getPostById(Long id) {
    log.info("Fetching post for id - "+id);
    return this.postRepository.findById(id)
            .switchIfEmpty(throwNoPostFoundException(id));
    }

    @Override
    public Mono<Post> addPost(PostInput postInput) {
        log.info("Adding post for "+postInput.toString());
        return this.postRepository
        .save(new Post(postInput))
        .onErrorMap(DuplicateKeyException.class, ex -> new DuplicatePostException("Post already exists!"));
    }

    @Override
    public Mono<Post> updatePost(Long id, PostInput postInput) {
        log.info("Updating post for id - {} with values - {}",id,postInput);
        return this.postRepository.findById(id).flatMap(post->{
            post.setUpdatedAt(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
            post.setAuthor(postInput.getAuthor());
            post.setTitle(postInput.getTitle());
            post.setContent(postInput.getContent());
            return this.postRepository.save(post);
        }).switchIfEmpty(throwNoPostFoundException(id));
    }

    @Override
    public Mono<Post> deletePost(Long id) {
        log.info("Deleting post for id - "+id);
        return this.postRepository
        .findById(id)
        .map(
            post -> {
              this.postRepository.deleteById(id).subscribe();
              return post;
            })
        .switchIfEmpty(throwNoPostFoundException(id));
    }
    private Mono<Post> throwNoPostFoundException(Long id){
        return Mono.error(new PostNotFoundException("No post found for id - "+id));
    }
}

