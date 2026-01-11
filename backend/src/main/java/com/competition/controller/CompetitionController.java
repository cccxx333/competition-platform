package com.competition.controller;

import com.competition.dto.CompetitionDTO;
import com.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CompetitionController {

    private final CompetitionService competitionService;

    /**
     * 获取竞赛列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<CompetitionDTO>> getCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompetitionDTO> competitions = competitionService.getCompetitions(pageable);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 根据ID获取竞赛详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompetitionDTO> getCompetitionById(@PathVariable Long id) {
        CompetitionDTO competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(competition);
    }

    /**
     * 搜索竞赛
     */
    @GetMapping("/search")
    public ResponseEntity<List<CompetitionDTO>> searchCompetitions(
            @RequestParam String keyword) {
        List<CompetitionDTO> competitions = competitionService.searchCompetitions(keyword);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 根据分类获取竞赛
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CompetitionDTO>> getCompetitionsByCategory(
            @PathVariable String category) {
        List<CompetitionDTO> competitions = competitionService.getCompetitionsByCategory(category);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 创建竞赛
     */
    @PostMapping
    public ResponseEntity<CompetitionDTO> createCompetition(
            @RequestBody CompetitionDTO competitionDTO) {
        CompetitionDTO created = competitionService.createCompetition(competitionDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * 获取可用竞赛（公开接口）
     */
    @GetMapping("/public/available")
    public ResponseEntity<List<CompetitionDTO>> getAvailableCompetitions() {
        List<CompetitionDTO> competitions = competitionService.getAvailableCompetitions()
                .stream()
                .map(competition -> {
                    CompetitionDTO dto = new CompetitionDTO();
                    // 转换逻辑
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(competitions);
    }
}