package com.cg.bms;

import com.cg.bms.exception.PostNotFoundException;
import com.cg.bms.model.PostData;
import com.cg.bms.service.PostServiceImpl;
import graphql.ErrorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import reactor.core.publisher.Flux;
import com.cg.bms.model.Post;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureGraphQlTester
class QueryTests {
    @Autowired
    GraphQlTester graphQlTester;
    @MockBean
    PostServiceImpl postService;
    @Test
    void getAllPosts_works_successfully(){
        when(postService.getAllPosts()).thenReturn(Flux.just(
            Post.builder().id(1L)
                            .title("title1")
                            .author("author1")
                            .content("content1")
                            .createdAt(OffsetDateTime.now())
                            .build(),
                Post.builder().id(2L)
                        .title("title2")
                        .author("author2")
                        .content("content2")
                        .createdAt(OffsetDateTime.now())
                        .build()
        ));
        graphQlTester.documentName("query/getAllPosts")
                        .execute()
                        .path("data.getAllPosts[*].title")
                        .entityList(String.class).contains("title1","title2");
        verify(postService,times(1)).getAllPosts();
    }
    @Test
    void getPosts_works_successfully() throws ExecutionException, InterruptedException {
        List<Post> postList = new ArrayList<>();
        postList.add(new Post(1L,"title1","content1","author1",OffsetDateTime.now(),null));
        postList.add(new Post(2L,"title2","content2","author2",OffsetDateTime.now(),OffsetDateTime.now()));
        when(postService.getPosts(any(PageRequest.class))).thenReturn(Mono.just(new PostData(postList,10,20L,30L,40)));
        PostData postData = graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",1)
                .variable("pageSize",2)
                .execute()
                .path("data.getPosts")
                .entity(PostData.class).get();
        Assertions.assertEquals(2,postData.getPosts().size());
        Assertions.assertEquals(1,postData.getPosts().get(0).getId());
        Assertions.assertEquals(2,postData.getPosts().get(1).getId());
        Assertions.assertEquals(10,postData.getSize());
        Assertions.assertEquals(20,postData.getTotalElements());
        Assertions.assertEquals(30,postData.getTotalPages());
        Assertions.assertEquals(40,postData.getNumber());
    }
    @Test
    void getPosts_throws_error_if_pageNumber_less_than_zero(){
        graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",-1)
                .variable("pageSize",1)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                   Assertions.assertEquals(1,responseErrors.size());
                   Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
                   Assertions.assertEquals("getPosts.pageNumber: must be greater than or equal to 0",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void getPosts_throws_error_if_pageSize_less_than_one(){
        graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",1)
                .variable("pageSize",-1)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    Assertions.assertEquals(1,responseErrors.size());
                    Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
                    Assertions.assertEquals("getPosts.pageSize: must be greater than or equal to 1",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void getPosts_throws_error_if_pageSize_more_then_hundred(){
        graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",1)
                .variable("pageSize",101)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    Assertions.assertEquals(1,responseErrors.size());
                    Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
                    Assertions.assertEquals("getPosts.pageSize: must be less than or equal to 100",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void getPosts_throws_exception_if_service_throws_exception() throws ExecutionException, InterruptedException {
        Mockito.doThrow(InterruptedException.class).when(postService).getPosts(any(PageRequest.class));
        graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",1)
                        .variable("pageSize",2)
                        .execute()
                        .errors()
                        .satisfy(responseErrors -> Assertions.assertEquals(
                                org.springframework.graphql.execution.ErrorType.INTERNAL_ERROR,responseErrors.get(0).getErrorType()));

    }
    @Test
    void getPostById_works_successfully(){
        when(postService.getPostById(any(Long.class))).thenReturn(Mono.just(
                Post.builder().id(100L)
                        .title("title100")
                        .author("author100")
                        .content("content100")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        ));
        graphQlTester.documentName("query/getPostById")
                .variable("id",1L)
                .execute()
                .path("data.getPostById")
                .entity(Post.class)
                .satisfies(post-> {
                    Assertions.assertEquals("title100",post.getTitle());
                    Assertions.assertEquals("author100",post.getAuthor());
                    Assertions.assertEquals("content100",post.getContent());
                });
    }
    @Test
    void getPostById_throws_error_if_no_post_found(){
        when(postService.getPostById(any(Long.class))).thenReturn(Mono.error(new PostNotFoundException("No post found for id")));
        graphQlTester.documentName("query/getPostById")
                .variable("id",1L)
                .execute()
                .errors()
                .satisfy(responseErrors->{
                    Assertions.assertEquals(1,responseErrors.size());
                    Assertions.assertEquals(ErrorType.DataFetchingException,responseErrors.get(0).getErrorType());
                    Assertions.assertEquals("No post found for id",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void getPostById_throws_error_for_empty_input_id(){
        graphQlTester.documentName("query/getPostById")
                .variable("id","")
                .execute()
                .errors()
                .satisfy(responseErrors->{
                    Assertions.assertEquals(1,responseErrors.size());
                    Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
                    Assertions.assertEquals("getPostById.id: id is required",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void getPostById_throws_error_for_invalid_id(){
        graphQlTester.documentName("query/getPostById")
                .variable("id","z")
                .execute()
                .errors()
                .satisfy(responseErrors ->Assertions.assertEquals(
                        org.springframework.graphql.execution.ErrorType.INTERNAL_ERROR,responseErrors.get(0).getErrorType()));
    }
}
