package com.aw.aidati.scoring;

import cn.hutool.json.JSONUtil;
import com.aw.aidati.model.dto.question.QuestionContentDTO;
import com.aw.aidati.model.entity.App;
import com.aw.aidati.model.entity.Question;
import com.aw.aidati.model.entity.ScoringResult;
import com.aw.aidati.model.entity.UserAnswer;
import com.aw.aidati.model.vo.QuestionVO;
import com.aw.aidati.service.QuestionService;
import com.aw.aidati.service.ScoringResultService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {
    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        // 1. 根据 id 查询到题目和题目结果信息（按分数降序排序）
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );

        // 2. 统计用户的总得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
//        System.out.println("@@@@@@@questionContent:"+questionContent);
        // 遍历答案列表
        int titleIndex = 0;
        for (String answer : choices) {
            // 遍历题目列表
            // 遍历题目中的选项
            for (QuestionContentDTO.Option option : questionContent.get(titleIndex).getOptions()) {
                // 如果答案和选项的key匹配
                if (option.getKey().equals(answer)) {
                    int score = Optional.of(option.getScore()).orElse(0);
//                    System.out.println("index:"+titleIndex+"optionKey:"+option.getKey()+"getScore:"+score);
                    totalScore += score;
                }
            }
            titleIndex++;
        }
        // 3. 遍历得分结果，找到第一个用户分数大于得分范围的结果，作为最终结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult scoringResult : scoringResultList) {
            if (totalScore >= scoringResult.getResultScoreRange()) {
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 4. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
