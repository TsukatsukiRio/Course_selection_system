package com.smartcampus.recommend.service;

import com.smartcampus.common.result.R;
import com.smartcampus.recommend.feign.CourseClient;
import com.smartcampus.recommend.feign.SelectionClient;
import com.smartcampus.recommend.feign.UserClient;
import com.smartcampus.recommend.vo.CourseVO;
import com.smartcampus.recommend.vo.GradeVO;
import com.smartcampus.recommend.vo.RecommendItemVO;
import com.smartcampus.recommend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 课程推荐服务（UC-10）：
 * 1. 基于内容的推荐：兴趣标签匹配、高分课程类型偏好、同院系课程；
 * 2. 协同过滤（Item-Based）：基于"选了同门课的学生还选了什么"的共现统计；
 * 3. 先修课程过滤：仅推荐先修要求已满足的课程；
 * 4. 冷启动兜底：新用户无历史数据时推荐同专业热门课程与通识课程；
 * 5. 服务降级：依赖服务不可用时返回热门课程兜底列表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserClient userClient;
    private final CourseClient courseClient;
    private final SelectionClient selectionClient;

    /** 个性化推荐主流程 */
    public List<RecommendItemVO> recommend(String studentNo) {
        UserVO user = fetchUser(studentNo);
        List<String> myCourseNos = safeList(selectionClient.studentCourseNos(studentNo));
        List<GradeVO> grades = safeGrades(studentNo);
        List<CourseVO> allCourses = fetchOpenCourses();

        // 高分课程类型偏好（平均分 >= 85 的类型）
        Map<String, Double> typeSum = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();
        for (GradeVO grade : grades) {
            if (grade.getCourseType() != null && grade.getScore() != null) {
                typeSum.merge(grade.getCourseType(), grade.getScore().doubleValue(), Double::sum);
                typeCount.merge(grade.getCourseType(), 1, Integer::sum);
            }
        }
        Set<String> preferredTypes = new HashSet<>();
        typeSum.forEach((type, sum) -> {
            if (typeCount.getOrDefault(type, 0) > 0
                    && sum / typeCount.get(type) >= 85) {
                preferredTypes.add(type);
            }
        });
        Set<String> passed = grades.stream()
                .filter(g -> g.getScore() != null && g.getScore().doubleValue() >= 60)
                .map(GradeVO::getCourseNo).collect(Collectors.toSet());

        // Item-Based 协同过滤：与我已选课程相关的共现统计
        Map<String, Integer> coOccurrence = buildCoOccurrence(myCourseNos);

        // 兴趣标签
        List<String> interests = new ArrayList<>();
        if (StringUtils.hasText(user.getInterestTags())) {
            for (String tag : user.getInterestTags().split("[,，]")) {
                if (StringUtils.hasText(tag)) {
                    interests.add(tag.trim());
                }
            }
        }

        List<RecommendItemVO> candidates = new ArrayList<>();
        for (CourseVO course : allCourses) {
            if (course.getStatus() == null || course.getStatus() != 1
                    || myCourseNos.contains(course.getCourseNo())) {
                continue;
            }
            // 名额已满不推荐
            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();
            int selected = course.getSelectedCount() == null ? 0 : course.getSelectedCount();
            if (capacity > 0 && selected >= capacity) {
                continue;
            }
            // 先修课程未满足不推荐（保持推荐可执行）
            if (StringUtils.hasText(course.getPrerequisite())) {
                boolean satisfied = true;
                for (String pre : course.getPrerequisite().split(",")) {
                    if (StringUtils.hasText(pre.trim()) && !passed.contains(pre.trim())) {
                        satisfied = false;
                        break;
                    }
                }
                if (!satisfied) {
                    continue;
                }
            }
            RecommendItemVO item = new RecommendItemVO();
            BeanUtils.copyProperties(course, item);
            item.setScore(0);
            scoreContentBased(item, course, user, interests, preferredTypes);
            int co = coOccurrence.getOrDefault(course.getCourseNo(), 0);
            item.setScore(item.getScore() + Math.min(4, co * 2.0));
            // 热度加权
            if (capacity > 0) {
                item.setScore(item.getScore() + Math.min(3, 3.0 * selected / capacity));
            }
            item.setReason(buildReason(item, course, user, interests, preferredTypes, co));
            candidates.add(item);
        }
        candidates.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<RecommendItemVO> top = candidates.stream().limit(10).collect(Collectors.toList());

        // 冷启动兜底：无任何候选时推荐热门课程
        if (top.isEmpty()) {
            log.info("学生 {} 无历史数据，使用热门课程冷启动推荐", studentNo);
            return hotFallback();
        }
        return top;
    }

    /** 热门课程兜底（冷启动 / 熔断降级共用） */
    public List<RecommendItemVO> hotFallback() {
        try {
            R<List<CourseVO>> r = courseClient.hot(10);
            if (r == null || r.getData() == null) {
                return List.of();
            }
            return r.getData().stream().map(course -> {
                RecommendItemVO item = new RecommendItemVO();
                BeanUtils.copyProperties(course, item);
                item.setScore(0);
                item.setReason("热门课程");
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("热门课程兜底查询失败", e);
            return List.of();
        }
    }

    /** 内容推荐打分 */
    private void scoreContentBased(RecommendItemVO item, CourseVO course, UserVO user,
                                   List<String> interests, Set<String> preferredTypes) {
        String haystack = (course.getName() + " " + course.getCollege() + " "
                + (course.getDescription() == null ? "" : course.getDescription())).toLowerCase();
        boolean interestMatched = false;
        for (String tag : interests) {
            if (haystack.contains(tag.toLowerCase())) {
                item.setScore(item.getScore() + 4);
                interestMatched = true;
            }
        }
        if (preferredTypes.contains(course.getCourseType())) {
            item.setScore(item.getScore() + 3);
        }
        if (user.getCollege() != null && user.getCollege().equals(course.getCollege())) {
            item.setScore(item.getScore() + 2);
        }
    }

    /** 推荐理由生成 */
    private String buildReason(RecommendItemVO item, CourseVO course, UserVO user,
                               List<String> interests, Set<String> preferredTypes, int coOccurrence) {
        List<String> reasons = new ArrayList<>();
        String haystack = (course.getName() + " " + course.getCollege() + " "
                + (course.getDescription() == null ? "" : course.getDescription())).toLowerCase();
        for (String tag : interests) {
            if (haystack.contains(tag.toLowerCase())) {
                reasons.add("匹配兴趣「" + tag + "」");
                break;
            }
        }
        if (preferredTypes.contains(course.getCourseType())) {
            reasons.add("你擅长的课程类型");
        }
        if (user.getCollege() != null && user.getCollege().equals(course.getCollege())) {
            reasons.add("同院系课程");
        }
        if (coOccurrence > 0) {
            reasons.add("与你相似的同学也选了这门课");
        }
        if (reasons.isEmpty()) {
            reasons.add("热门课程");
        }
        return String.join("，", reasons);
    }

    /** Item-Based 协同过滤共现矩阵（限采样控制调用量） */
    private Map<String, Integer> buildCoOccurrence(List<String> myCourseNos) {
        Map<String, Integer> coOccurrence = new HashMap<>();
        int sampleCourses = Math.min(myCourseNos.size(), 3);
        int sampledStudents = 0;
        for (int i = 0; i < sampleCourses; i++) {
            List<String> students = safeList(selectionClient.courseStudentNos(myCourseNos.get(i)));
            for (String student : students) {
                if (sampledStudents >= 20) {
                    return coOccurrence;
                }
                sampledStudents++;
                List<String> otherCourses = safeList(selectionClient.studentCourseNos(student));
                for (String courseNo : otherCourses) {
                    if (!myCourseNos.contains(courseNo)) {
                        coOccurrence.merge(courseNo, 1, Integer::sum);
                    }
                }
            }
        }
        return coOccurrence;
    }

    private UserVO fetchUser(String studentNo) {
        try {
            R<UserVO> r = userClient.getByUsername(studentNo);
            if (r != null && r.getData() != null) {
                return r.getData();
            }
        } catch (Exception e) {
            log.warn("用户服务调用失败，使用空画像", e);
        }
        return new UserVO();
    }

    private List<CourseVO> fetchOpenCourses() {
        try {
            R<List<CourseVO>> r = courseClient.allOpen();
            if (r != null && r.getData() != null) {
                return r.getData();
            }
        } catch (Exception e) {
            log.warn("课程服务调用失败", e);
        }
        return List.of();
    }

    private List<GradeVO> safeGrades(String studentNo) {
        try {
            R<List<GradeVO>> r = courseClient.grades(studentNo);
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.warn("成绩查询失败", e);
            return List.of();
        }
    }

    private List<String> safeList(R<List<String>> r) {
        return r != null && r.getData() != null ? r.getData() : List.of();
    }
}
