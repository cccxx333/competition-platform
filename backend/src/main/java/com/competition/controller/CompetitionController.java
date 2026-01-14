package com.competition.controller;

import com.competition.dto.CompetitionCreateRequest;
import com.competition.dto.CompetitionResponse;
import com.competition.dto.CompetitionUpdateRequest;
import com.competition.entity.Competition;
import com.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CompetitionController {

    private final CompetitionService competitionService;

    /**
     * 鑾峰彇绔炶禌鍒楄〃锛堝垎椤碉級
     */
    @GetMapping
    public ResponseEntity<Page<CompetitionResponse>> getCompetitions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Competition.CompetitionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CompetitionResponse> competitions = competitionService.getCompetitions(pageable, name, status);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 鏍规嵁ID鑾峰彇绔炶禌璇︽儏
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getCompetitionById(@PathVariable Long id) {
        CompetitionResponse competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(competition);
    }

    /**
     * 鎼滅储绔炶禌
     */
    @GetMapping("/search")
    public ResponseEntity<List<CompetitionResponse>> searchCompetitions(
            @RequestParam String keyword) {
        List<CompetitionResponse> competitions = competitionService.searchCompetitions(keyword);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 鏍规嵁鍒嗗垎绫昏幏鍙栫珵璧?
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CompetitionResponse>> getCompetitionsByCategory(
            @PathVariable String category) {
        List<CompetitionResponse> competitions = competitionService.getCompetitionsByCategory(category);
        return ResponseEntity.ok(competitions);
    }

    /**
     * 鍒涘缓绔炶禌
     */
    @PostMapping
    public ResponseEntity<CompetitionResponse> createCompetition(
            @Valid @RequestBody CompetitionCreateRequest request) {
        CompetitionResponse created = competitionService.createCompetition(request);
        return ResponseEntity.ok(created);
    }

    /**
     * 鏇存柊绔炶禌
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> updateCompetition(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionUpdateRequest request) {
        CompetitionResponse updated = competitionService.updateCompetition(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 鑾峰彇鍙敤绔炶禌锛堝叕寮€鎺ュ彛锛?
     */
    @GetMapping("/public/available")
    public ResponseEntity<List<CompetitionResponse>> getAvailableCompetitions() {
        List<CompetitionResponse> competitions = competitionService.getAvailableCompetitions();
        return ResponseEntity.ok(competitions);
    }
}
