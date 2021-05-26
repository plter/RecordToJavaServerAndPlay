package com.example.demo;

import org.apache.catalina.webresources.FileResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
public class DownloadController {

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> download(@RequestParam(name = "filename") String filename) {

        File f = new File(filename);
        Resource file = new FileSystemResource(f);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}