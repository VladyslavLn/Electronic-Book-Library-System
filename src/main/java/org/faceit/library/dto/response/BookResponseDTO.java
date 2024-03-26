package org.faceit.library.dto.response;

import lombok.Data;

@Data
public class BookResponseDTO {
    private Integer id;
    private String name;
    private String author;
    private String language;
    private String fileKey;
}
