package com.cg.bms;

import com.cg.bms.exception.DuplicatePostException;
import com.cg.bms.exception.PostNotFoundException;
import com.cg.bms.model.Post;
import com.cg.bms.model.PostData;
import com.cg.bms.model.PostInput;
import com.cg.bms.repository.PostRepository;
import com.cg.bms.service.PostServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class PostServiceImplTest {
    @Mock
    PostRepository postRepository;
    @InjectMocks
    PostServiceImpl postService;
    @Test
    void getAllPosts_should_return_all_posts(){
        List<Post> postList = new ArrayList<>();
        postList.add(new Post(1L,"title1","content1","author1", OffsetDateTime.now(),null));
        postList.add(new Post(2L,"title2","content2","author2",OffsetDateTime.now(),OffsetDateTime.now()));
        when(postRepository.findAll()).thenReturn(Flux.just(postList.get(0),postList.get(1)));
        List<Post> response = postService.getAllPosts().collectList().flatMap(Mono::just).block();
        Assertions.assertEquals(response,postList);
    }
    @Test
    void getPosts_should_return_post_data_for_corresponding_page() throws ExecutionException, InterruptedException {
        List<Post> postList = new ArrayList<>();
        postList.add(new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null));
        postList.add(new Post(2L,"title2","content2","author2",OffsetDateTime.now(),OffsetDateTime.now()));
        when(postRepository.findAllBy(any(PageRequest.class))).thenReturn(Flux.just(postList.get(0),postList.get(1)));
        when(postRepository.count()).thenReturn(Mono.just(2L));
        PostData response = postService.getPosts(PageRequest.of(1,3,Sort.by("id"))).block();
        Assertions.assertEquals(2,response.getPosts().size());
        Assertions.assertEquals(1,response.getPosts().get(0).getId());
        Assertions.assertEquals(2,response.getPosts().get(1).getId());
        Assertions.assertEquals(1,response.getNumber());
        Assertions.assertEquals(3,response.getSize());
        Assertions.assertEquals(2,response.getTotalElements());
        Assertions.assertEquals(1,response.getTotalPages());
    }

    @Test
    void getPostById_should_return_post_with_specified_id(){
        Post post = new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null);
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.just(post));
        StepVerifier.create(postService.getPostById(2L))
                .expectNext(post)
                .verifyComplete();
    }
    @Test
    void getPostById_should_throw_NoPostFoundException_if_no_post_found(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.empty());
        StepVerifier.create(postService.getPostById(1L))
                .expectErrorMatches(throwable->throwable instanceof PostNotFoundException)
                .verify();
    }
    @Test
    void addPost_should_add_new_post(){
        Post post = new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null);
        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(post));
        PostInput postInput = new PostInput("title100","content100","author100");
        StepVerifier.create(postService.addPost(postInput))
                .expectNext(post)
                .verifyComplete();
    }
    @Test
    void addPost_should_throw_DuplicatePostException_if_post_already_exists(){
        when(postRepository.save(any(Post.class))).thenReturn(Mono.error(new DuplicateKeyException("Post Exists")));
        PostInput postInput = new PostInput("title100","content100","author100");
        StepVerifier.create(postService.addPost(postInput))
                .expectErrorMatches(throwable->throwable instanceof DuplicatePostException)
                .verify();
    }
    @Test
    void updatePost_should_successfully_update_post(){
        Post post1 = new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null);
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.just(post1));
        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(post1));
        PostInput postInput = new PostInput("title100","content100","author100");
        StepVerifier.create(postService.updatePost(3L,postInput))
                .expectNext(post1)
                .verifyComplete();
    }
    @Test
    void updatePost_should_throw_PostNotFoundException_if_post_not_found(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.empty());
        PostInput postInput = new PostInput("title100","content100","author100");
        StepVerifier.create(postService.updatePost(1L,postInput))
                .expectErrorMatches(throwable->throwable instanceof PostNotFoundException)
                .verify();
    }
    @Test
    void deletePost_should_successfully_delete_post(){
        Post post = new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null);
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.just(post));
        when(postRepository.deleteById(any(Long.class))).thenReturn(Mono.empty());
        StepVerifier.create(postService.deletePost(2L))
                .expectNext(post)
                .verifyComplete();
    }
    @Test
    void deletePost_should_throw_PostNotFoundException_if_post_not_found(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.empty());
        StepVerifier.create(postService.deletePost(1L))
                .expectErrorMatches(throwable->throwable instanceof PostNotFoundException)
                .verify();
    }
}
