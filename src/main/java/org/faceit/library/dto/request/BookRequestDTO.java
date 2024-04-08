package org.faceit.library.dto.request;

import lombok.Data;

@Data
public class BookRequestDTO {
    private String title;
    private String author;
    private String language;
}
