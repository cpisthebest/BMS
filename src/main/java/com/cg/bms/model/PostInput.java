package com.cg.bms.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostInput {

    @NotBlank(message = "{postInput.title.not.blank}")
    @Size(min=1,max=20)
    private String title;
    @Size(max=1000000000)
    private String content;
    @NotBlank(message = "{postInput.author.not.blank}")
    @Size(min=1,max=10)
    private String author;

}
