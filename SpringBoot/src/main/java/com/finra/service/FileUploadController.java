package com.finra.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finra.objects.FileMetaData;
import com.finra.storage.StorageFileNotFoundException;
import com.finra.storage.StorageService;
import com.google.gson.Gson;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    
    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {
    	    	   	  	
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        Resource rFile = storageService.loadAsResource(file.getOriginalFilename());
        
        BasicFileAttributes attr = Files.readAttributes(rFile.getFile().toPath(), BasicFileAttributes.class);
        
        storageService.write(file.getOriginalFilename(), storageService.extractFileMetaData(attr));
        
        return "redirect:/";
    }
    
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

    
    @GetMapping("/metadata/{filename:.+}")
    @ResponseBody
    public ResponseEntity<String> getMetaDataFile(@PathVariable String filename) throws IOException {

        Resource file = storageService.loadAsResource(filename);
        
        BasicFileAttributes attr = Files.readAttributes(file.getFile().toPath(), BasicFileAttributes.class);
        
        String data = storageService.extractFileMetaData(attr);
        
        FileMetaData fileMetaData = storageService.extractFileMetaDataJson(attr);
        
        Gson g = new Gson();
        
        String jsonData = g.toJson(fileMetaData);
        
        return ResponseEntity.ok(jsonData);
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
