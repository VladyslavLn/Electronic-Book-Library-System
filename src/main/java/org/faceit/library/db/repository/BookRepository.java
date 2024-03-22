package org.faceit.library.db.repository;

import org.faceit.library.db.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query(value = "SELECT files.file_key FROM files WHERE files.upload_user_id = :userId", nativeQuery = true)
    List<String> getDownloadedBooksByUserId(Integer userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO files(file_key, upload_user_id) VALUES (:originalFilename, :userId)", nativeQuery = true)
    void saveFilesMetadata(@Param("userId") Integer userId, @Param("originalFilename") String originalFilename);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM files WHERE files.upload_user_id = :userId AND file_key = :fileKey", nativeQuery = true)
    void deleteFilesMetadata(@Param("userId") Integer userId, @Param("fileKey") String fileKey);
}
