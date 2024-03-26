package org.faceit.library.dto.request;

import lombok.Data;

@Data
public class BookRequestDTO {
    private String name;
    private String author;
    private String language;
}
