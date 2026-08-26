package com.smartcampus.course.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.course.dto.CourseDTO;
import com.smartcampus.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员课程管理接口（UC-04）。
 */
@RestController
@RequestMapping("/api/course/admin")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    @PostMapping
    public R<Void> create(@Valid @RequestBody CourseDTO dto) {
        courseService.create(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        courseService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateStatus(id, status);
        return R.ok();
    }
}
