package com.finra.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.finra.objects.FileMetaData;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);
    
    void write(String fileName, String data);
    
    String extractFileMetaData(BasicFileAttributes attr);
    
    FileMetaData extractFileMetaDataJson(BasicFileAttributes attr);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
