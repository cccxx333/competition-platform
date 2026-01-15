package com.competition.controller;

import com.competition.dto.TeacherApplicationReviewRequest;
import com.competition.entity.TeacherApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/teacher-applications")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminTeacherApplicationController {

    @GetMapping
    /**
     * 管理员查看教师申请列表
     * 默认排序：appliedAt desc
     * 返回体契约：TeacherApplicationResponse 列表
     */
    public ResponseEntity<String> getApplications(
            @RequestParam(required = false) TeacherApplication.Status status,
            @RequestParam(required = false) Long competitionId) {
        return ResponseEntity.ok("接口已定义，业务逻辑待实现（默认排序：appliedAt desc）");
    }

    @PutMapping("/{id}/review")
    /**
     * 管理员审核教师申请
     * 返回体契约：TeacherApplicationResponse
     */
    public ResponseEntity<String> reviewApplication(
            @PathVariable Long id,
            @RequestBody TeacherApplicationReviewRequest request) {
        return ResponseEntity.ok("接口已定义，业务逻辑待实现");
    }
}
