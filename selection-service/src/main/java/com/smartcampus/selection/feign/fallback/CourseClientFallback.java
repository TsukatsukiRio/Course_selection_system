package com.smartcampus.selection.feign.fallback;

import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.common.result.ResultCode;
import com.smartcampus.selection.feign.CourseClient;
import com.smartcampus.selection.vo.CourseVO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 课程服务 Feign 降级工厂：课程服务不可用时返回友好错误提示。
 */
@Component
public class CourseClientFallback implements FallbackFactory<CourseClient> {

    @Override
    public CourseClient create(Throwable cause) {
        return new CourseClient() {
            @Override
            public R<CourseVO> getByCourseNo(String courseNo) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }

            @Override
            public R<List<CourseVO>> batch(String courseNos) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }

            @Override
            public R<Boolean> decrement(String courseNo, String internal) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }

            @Override
            public R<Boolean> increment(String courseNo, String internal) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }

            @Override
            public R<List<String>> passedCourseNos(String studentNo) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }

            @Override
            public R<List<CourseVO>> hot(int limit) {
                throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "课程服务暂时不可用，请稍后重试");
            }
        };
    }
}
