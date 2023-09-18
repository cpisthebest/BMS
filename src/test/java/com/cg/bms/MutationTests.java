package com.cg.bms;

import com.cg.bms.model.Post;
import com.cg.bms.repository.PostRepository;
import graphql.ErrorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureGraphQlTester
@WithMockUser(authorities = "SCOPE_profile")
class MutationTests {
    @Autowired
    GraphQlTester graphQlTester;
    @MockBean
    PostRepository postRepository;
    private final Map<String,Object> postInputVariable = Map.of("title","inputTitle",
            "author","inputAuth",
            "content","inputContent");
    private final Map<String,Object> emptyTitlePostInputVariable = Map.of("title","",
            "author","inputAuth",
            "content","inputContent");
    private final Map<String,Object> emptyAuthorPostInputVariable = Map.of("title","inputTitle",
            "author","",
            "content","inputContent");
    @Test
    void addPost_successfully(){
        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(
                Post.builder().id(1L)
                        .title("addTitle")
                        .author("addAuthor")
                        .content("addContent")
                        .createdAt(OffsetDateTime.now())
                        .build()
        ));
        graphQlTester.documentName("mutation/addPost")
                .variable("postInput",postInputVariable)
                .execute()
                .path("addPost")
                .entity(Post.class).satisfies(
                        post->{
                            Assertions.assertEquals("addTitle",post.getTitle());
                            Assertions.assertEquals("addAuthor",post.getAuthor());
                            Assertions.assertEquals("addContent",post.getContent());
                        }
                );
        verify(postRepository,times(1)).save(any(Post.class));
    }

    @Test
    void addPost_throws_DuplicatePostException_if_post_exists(){
        when(postRepository.save(any(Post.class))).thenReturn(Mono.error(new DuplicateKeyException("Id exists")));
        graphQlTester.documentName(("mutation/addPost"))
                .variable("postInput",postInputVariable)
                .execute()
                .errors().satisfy(responseErrors -> {
                        Assertions.assertEquals(1,responseErrors.size());
                        Assertions.assertEquals(ErrorType.DataFetchingException,responseErrors.get(0).getErrorType());
                        Assertions.assertEquals("Post already exists!",responseErrors.get(0).getMessage());
                });
    }
    @Test
    void addPost_throws_validation_error_if_input_title_is_empty(){
        graphQlTester.documentName("mutation/addPost")
                .variable("postInput",emptyTitlePostInputVariable)
                .execute()
                .errors().satisfy(responseErrors ->emptyTitlePostInputAssertions(responseErrors,"addPost"));
    }
    @Test
    void addPost_throws_validation_error_if_input_content_is_large(){
        char[] inputContent = new char[1000000001];
        Arrays.fill(inputContent,'a');
        String inputContentString = new String(inputContent);
        Map<String,Object> postInputVariable = Map.of("title","inputTitle",
                                                      "author","inputAuth",
                                                      "content",inputContentString);
        graphQlTester.documentName("mutation/addPost")
                .variable("postInput",postInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors ->largeContentPostInputAssertions(responseErrors,"addPost"));
    }
    @Test
    void addPost_throws_validation_error_if_input_author_is_empty(){
        graphQlTester.documentName("mutation/addPost")
                .variable("postInput",emptyAuthorPostInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors -> emptyAuthorPostInputAssertions(responseErrors,"addPost"));
    }
    @Test
    void updatePost_works_successfully(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.just(
                Post.builder().id(2L)
                        .title("findTitle")
                        .author("findAuth")
                        .content("findContent")
                        .createdAt(OffsetDateTime.now())
                        .build()
        ));
        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(
                Post.builder().id(1L)
                        .title("title")
                        .author("author")
                        .content("content")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        ));
        graphQlTester.documentName("mutation/updatePost")
                .variable("id",4L)
                .variable("postInput",postInputVariable)
                .execute()
                .path("updatePost")
                .entity(Post.class)
                .satisfies(post->{
                    Assertions.assertEquals("title",post.getTitle());
                    Assertions.assertEquals("content",post.getContent());
                    Assertions.assertEquals("author",post.getAuthor());
                });

    }
    @Test
    void updatePost_throws_error_if_no_post_found(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.empty());
        graphQlTester.documentName("mutation/updatePost")
                .variable("id",6L)
                .variable("postInput",postInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors -> noPostFoundExceptionAssertions(responseErrors,6L) );
    }
    @Test
    void updatePost_throws_error_for_empty_input_id(){
        graphQlTester.documentName("mutation/updatePost")
                .variable("id","")
                .variable("postInput",postInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors -> emptyInputIdAssertions(responseErrors,"updatePost") );
    }
    @Test
    void updatePost_throws_error_for_empty_title(){
        graphQlTester.documentName("mutation/updatePost")
                .variable("id",1L)
                .variable("postInput",emptyTitlePostInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors->emptyTitlePostInputAssertions(responseErrors,"updatePost"));
    }
    @Test
    void updatePost_throws_error_for_large_content(){
        char[] inputContent = new char[1000000001];
        Arrays.fill(inputContent,'a');
        Map<String,Object> largeContentPostInputVariable = Map.of("title","inputTitle",
                "author","inputAuth",
                "content",new String(inputContent)
        );
        graphQlTester.documentName("mutation/updatePost")
                .variable("id",1L)
                .variable("postInput",largeContentPostInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors->largeContentPostInputAssertions(responseErrors,"updatePost"));
    }
    @Test
    void updatePost_throws_error_for_empty_author_input(){
        graphQlTester.documentName("mutation/updatePost")
                .variable("id",4L)
                .variable("postInput",emptyAuthorPostInputVariable)
                .execute()
                .errors()
                .satisfy(responseErrors->emptyAuthorPostInputAssertions(responseErrors,"updatePost"));
    }
    @Test
    void deletePost_works_successfully(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.just(
                Post.builder().id(1L)
                        .title("deleteTitle")
                        .author("delAuth")
                        .content("deleteAuth")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        ));
        when(postRepository.deleteById(any(Long.class))).thenReturn(Mono.empty());
        graphQlTester.documentName("mutation/deletePost")
                .variable("id",4L)
                .execute()
                .path("deletePost")
                .entity(Post.class)
                .satisfies(post->Assertions.assertEquals("deleteTitle",post.getTitle()));
    }
    @Test
    void deletePost_throws_error_if_no_post_found(){
        when(postRepository.findById(any(Long.class))).thenReturn(Mono.empty());
        graphQlTester.documentName("mutation/deletePost")
                .variable("id",4L)
                .execute()
                .errors()
                .satisfy(responseErrors -> noPostFoundExceptionAssertions(responseErrors,4L));
    }
    @Test
    void deletePost_throws_error_for_empty_input_id(){
        graphQlTester.documentName("mutation/deletePost")
                .variable("id","")
                .execute()
                .errors()
                .satisfy(responseErrors -> emptyInputIdAssertions(responseErrors,"deletePost"));
    }
    private void emptyTitlePostInputAssertions(List<ResponseError> responseErrors,String operationName){
            Assertions.assertEquals(1,responseErrors.size());
            Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
            Assertions.assertTrue(Objects.requireNonNull(responseErrors.get(0).getMessage()).contains(operationName+".postInput.title: title is required"));
            Assertions.assertTrue(Objects.requireNonNull(responseErrors.get(0).getMessage()).contains(operationName+".postInput.title: size must be between 1 and 20"));
    }
    private void largeContentPostInputAssertions(List<ResponseError> responseErrors,String operationName){
        Assertions.assertEquals(1,responseErrors.size());
        Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
        Assertions.assertTrue(Objects.requireNonNull(responseErrors.get(0).getMessage())
                .contains(operationName+".postInput.content: size must be between 0 and 1000000000"));
    }
    private void emptyAuthorPostInputAssertions(List<ResponseError> responseErrors,String operationName){
        Assertions.assertEquals(1,responseErrors.size());
        Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
        Assertions.assertTrue(Objects.requireNonNull(responseErrors.get(0).getMessage())
                .contains(operationName+".postInput.author: author is required"));
        Assertions.assertTrue(Objects.requireNonNull(responseErrors.get(0).getMessage())
                .contains(operationName+".postInput.author: size must be between 1 and 10"));
    }
    private void noPostFoundExceptionAssertions(List<ResponseError> responseErrors,Long id){
        Assertions.assertEquals(1,responseErrors.size());
        Assertions.assertEquals(ErrorType.DataFetchingException,responseErrors.get(0).getErrorType());
        Assertions.assertEquals("No post found for id - "+id,responseErrors.get(0).getMessage());
    }
    private void emptyInputIdAssertions(List<ResponseError> responseErrors,String operationName){
        Assertions.assertEquals(1,responseErrors.size());
        Assertions.assertEquals(ErrorType.ValidationError,responseErrors.get(0).getErrorType());
        Assertions.assertEquals(operationName+".id: id is required",responseErrors.get(0).getMessage());
    }
}
