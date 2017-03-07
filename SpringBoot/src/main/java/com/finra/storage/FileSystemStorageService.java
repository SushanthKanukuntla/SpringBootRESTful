package com.finra.storage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.finra.objects.FileMetaData;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
    
    @Override
    public void write(String fileName, String data) {
        try {
            if (data.isEmpty()) {
                throw new StorageException("Failed to write empty data " + data);
            }
            Files.write(this.rootLocation.resolve(fileName+"-Metadata.txt"), data.getBytes());
        } catch (IOException e) {
            throw new StorageException("Failed to write file ", e);
        }
    }
    
    @Override
    public String extractFileMetaData(BasicFileAttributes attr) {
        if (attr == null) {
            throw new StorageException("BasicFileAttributes is null " + attr);
        }
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" lastAccessTime: " + attr.lastAccessTime());
        stringBuilder.append("\n");
        stringBuilder.append(" isDirectory: " + attr.isDirectory());
        stringBuilder.append("\n");
        stringBuilder.append(" isOther: " + attr.isOther());
        stringBuilder.append("\n");
        stringBuilder.append(" isRegularFile: " + attr.isRegularFile());
        stringBuilder.append("\n");
        stringBuilder.append(" isSymbolicLink: " + attr.isSymbolicLink());
        stringBuilder.append("\n");
        stringBuilder.append(" size: " + attr.size());
        
        return stringBuilder.toString();
    }
    
    @Override
    public FileMetaData extractFileMetaDataJson(BasicFileAttributes attr) {
        if (attr == null) {
            throw new StorageException("BasicFileAttributes is null " + attr);
        }
        
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setIsDirectory(String.valueOf(attr.isDirectory()));
        fileMetaData.setIsOther(String.valueOf(attr.isOther()));
        fileMetaData.setIsRegularFile(String.valueOf(attr.isRegularFile()));
        fileMetaData.setIsSymbolicLink(String.valueOf(attr.isSymbolicLink()));
        fileMetaData.setLastAccessTime(String.valueOf(attr.lastAccessTime()));
        fileMetaData.setSize(String.valueOf(attr.size()));
        
        return fileMetaData;
     }


    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
