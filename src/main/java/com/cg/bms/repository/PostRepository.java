package com.cg.bms.repository;

import com.cg.bms.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
    Flux<Post> findAllBy(Pageable pageable);
}
