package com.aw.aidati.controller;

import cn.hutool.core.io.FileUtil;
import com.aw.aidati.common.BaseResponse;
import com.aw.aidati.common.ErrorCode;
import com.aw.aidati.common.ResultUtils;
import com.aw.aidati.constant.FileConstant;
import com.aw.aidati.exception.BusinessException;
import com.aw.aidati.exception.ThrowUtils;
import com.aw.aidati.manager.CosManager;
import com.aw.aidati.mapper.UserAnswerMapper;
import com.aw.aidati.model.dto.file.UploadFileRequest;
import com.aw.aidati.model.dto.statistic.AppAnswerCountDTO;
import com.aw.aidati.model.dto.statistic.AppAnswerResultCountDTO;
import com.aw.aidati.model.entity.User;
import com.aw.aidati.model.enums.FileUploadBizEnum;
import com.aw.aidati.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 热门应用及回答数统计（top 10）
     *
     * @return
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    /**
     * 某应用回答结果分布统计
     *
     * @param appId
     * @return
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
