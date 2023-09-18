package com.cg.bms;

import com.cg.bms.model.Post;
import com.cg.bms.model.PostData;
import com.cg.bms.service.PostServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(authorities = "SCOPE_profile")
class PostGraphQLControllerIntTest {
    @Autowired
    GraphQlTester graphQlTester;
    @Autowired
    PostServiceImpl postService;
    @ParameterizedTest
    @ValueSource(strings={"Test1","Test2","Test3","Test4","Test5","Test6","Test7","Test8"})
    @Order(1)
    void shouldAddNewPost(String input){
    long currentPostCount=postService.getAllPosts().toStream().count();
    System.out.println("Number of posts before addPost test execution - "+currentPostCount);
    Map<String,Object> postInputVariable = Map.of(
                    "title","title"+input,
                    "content","content"+input,
                    "author","auth"+input
    );
    graphQlTester.documentName("mutation/addPost")
            .variable("postInput",postInputVariable)
            .execute()
            .path("addPost")
            .entity(Post.class)
            .satisfies(post->{
                assertNotNull(post.getId());
                assertEquals("title"+input,post.getTitle());
                assertEquals("content"+input,post.getContent());
                assertEquals("auth"+input,post.getAuthor());
            });

    assertEquals(currentPostCount+1,postService.getAllPosts().toStream().count());
    }
    @Test
    @Order(2)
    void testGetAllPostsShouldReturnAllPosts(){
        graphQlTester.documentName("query/getAllPosts")
                .execute()
                .path("getAllPosts")
                .entityList(Post.class)
                .satisfies(postList->assertEquals(8,postList.size()));
    }
    @Test
    @Order(3)
    void testGetPostByIdShouldReturnPostSuccessfully(){
    graphQlTester.documentName("query/getPostById")
            .variable("id",1L)
            .execute()
            .path("getPostById")
            .entity(Post.class)
            .satisfies(post-> {
                assertEquals(1L, post.getId());
                assertEquals("titleTest1", post.getTitle());
                assertEquals("contentTest1",post.getContent());
                assertEquals("authTest1",post.getAuthor());
            });
    }
    @Test
    @Order(4)
    void testUpdatePostShouldUpdatePostSuccessfully(){
    Map<String,Object> postInputVariable = Map.of(
            "title","UpdatedTitle",
            "author","UpdAuthor",
            "content","UpdatedContent"
    );
    graphQlTester.documentName("mutation/updatePost")
            .variable("id",1L)
            .variable("postInput",postInputVariable)
            .execute()
            .path("updatePost")
            .entity(Post.class)
            .satisfies(post->{
                assertEquals(1L,post.getId());
                assertEquals("UpdatedTitle",post.getTitle());
                assertEquals("UpdAuthor",post.getAuthor());
                assertEquals("UpdatedContent",post.getContent());
            });
    }
    @Test
    @Order(5)
    void testDeletePostShouldDeletePostSuccessfully(){
    long currentPostCount = postService.getAllPosts().toStream().count();
    System.out.println("Number of total posts before deletePost test execution - "+currentPostCount);
    graphQlTester.documentName("mutation/deletePost")
            .variable("id",1L)
            .execute()
            .path("deletePost")
            .entity(Post.class)
            .satisfies(post->assertEquals(1L,post.getId()));
    assertEquals(currentPostCount-1,postService.getAllPosts().toStream().count());
    }
    @Test
    @Order(6)
    void testGetPostsShouldReturnPostByPageNumberAndPageSize(){
        graphQlTester.documentName("query/getPosts")
                .variable("pageNumber",2)
                .variable("pageSize",2)
                .execute()
                .path("getPosts")
                .entity(PostData.class)
                .satisfies(postData -> {
                    System.out.println(postData.toString());
                   assertEquals(2,postData.getPosts().size());
                   assertEquals(6,postData.getPosts().get(0).getId());
                   assertEquals(7,postData.getPosts().get(1).getId());
                   assertEquals(2,postData.getSize());
                   assertEquals(7,postData.getTotalElements());
                   assertEquals(4,postData.getTotalPages());
                   assertEquals(2,postData.getNumber());
                });
    }
}
