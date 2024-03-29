package org.faceit.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookFileMetadata {
    private String fileName;
    private byte[] fileData;
}
