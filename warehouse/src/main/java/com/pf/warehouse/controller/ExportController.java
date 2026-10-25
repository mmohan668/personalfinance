package com.pf.warehouse.controller;

import com.pf.common.dto.export.ExportRequest;
import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.dto.gp.GridResult;
import com.pf.common.service.export.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportController {
    private final ExportService exportService;

    @PostMapping("/exportServer")
    public ApiResponse exportServer(@RequestBody ExportRequest exportRequest) {
        return exportService.exportServer(exportRequest);
    }

    @PostMapping("/fetchExports")
    public GridResult fetchExports() throws IOException {
        return exportService.getFilesByUsername();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filePath) throws IOException {
        Resource resource = exportService.downloadFile(filePath);
        String fileName = resource.getFilename();
        return ResponseEntity.ok()
                .contentType(
                        org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition
                                .attachment()
                                .filename(fileName,
                                        java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(resource);
    }
}
