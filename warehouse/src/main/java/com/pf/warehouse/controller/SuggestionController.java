package com.pf.warehouse.controller;

import com.pf.common.dto.suggestion.SuggestionRequest;
import com.pf.common.service.suggestion.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/suggestion")
@RequiredArgsConstructor
public class SuggestionController {
    private final SuggestionService suggestionService;

    @PostMapping("/fetchSuggestion")
    public List<?> fetchSuggestion(@RequestBody SuggestionRequest suggestionRequest) {
        return suggestionService.fetchSuggestions(suggestionRequest);
    }
}
