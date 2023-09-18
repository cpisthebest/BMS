package com.cg.bms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@Data
@AllArgsConstructor
public class PostData {
    private List<Post> posts;
    private Integer size;
    private Long totalElements;
    private Long totalPages;
    private Integer number;
}
