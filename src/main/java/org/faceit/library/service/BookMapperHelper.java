package org.faceit.library.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMapperHelper {
    private final S3Service s3Service;

    @Named("mapCover")
    public byte[] mapCover(String bookCover) {
        if (StringUtils.isBlank(bookCover)) {
            return null;
        }
        return s3Service.getBookCover(bookCover);
    }
}
