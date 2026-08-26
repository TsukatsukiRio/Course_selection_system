package com.smartcampus.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.result.R;
import com.smartcampus.course.entity.TeachingPlan;
import com.smartcampus.course.service.CourseService;
import com.smartcampus.course.service.TeachingPlanService;
import com.smartcampus.course.vo.CourseDetailVO;
import com.smartcampus.course.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程查询接口（UC-05）：组合检索、详情、选项、热门。
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final TeachingPlanService teachingPlanService;

    @GetMapping("/page")
    public R<Page<CourseVO>> page(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String courseNo,
                                  @RequestParam(required = false) String teacherName,
                                  @RequestParam(required = false) String college,
                                  @RequestParam(required = false) String courseType,
                                  @RequestParam(required = false) Integer minCredit,
                                  @RequestParam(required = false) Integer maxCredit,
                                  @RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(courseService.pageQuery(name, courseNo, teacherName, college, courseType,
                minCredit, maxCredit, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public R<CourseDetailVO> detail(@PathVariable Long id) {
        CourseDetailVO vo = new CourseDetailVO();
        BeanUtils.copyProperties(courseService.getById(id), vo);
        vo.setTeachingPlan(teachingPlanService.getByCourseId(id));
        return R.ok(vo);
    }

    @GetMapping("/options")
    public R<List<CourseVO>> options(@RequestParam(required = false) String keyword) {
        return R.ok(courseService.options(keyword));
    }

    /** 热门课程（网关白名单放行，供首页展示） */
    @GetMapping("/hot")
    public R<List<CourseVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(courseService.hot(limit));
    }

    /** 查看课程教学计划详情 */
    @GetMapping("/{id}/teaching-plan")
    public R<TeachingPlan> teachingPlan(@PathVariable Long id) {
        return R.ok(teachingPlanService.getByCourseId(id));
    }
}
