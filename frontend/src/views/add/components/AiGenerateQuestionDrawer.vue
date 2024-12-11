<template>
  <a-button type="outline" @click="handleClick">AI 生成题目</a-button>
  <a-drawer
    :width="340"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    unmountOnClose
  >
    <template #title>AI 生成题目</template>
    <div>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
      >
        <a-form-item label="应用 id">
          {{ appId }}
        </a-form-item>
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number
            min="1"
            max="20"
            v-model="form.questionNumber"
            placeholder="请输入题目数量"
          />
        </a-form-item>
        <a-form-item field="trueNumber" label="布置题数">
          <a-input-number
              min="1"
              :max="form.questionNumber"
              v-model="trueNumber"
              placeholder="请输入布置题数"
          />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数量">
          <a-input-number
            min="0"
            max="6"
            v-model="form.optionNumber"
            placeholder="请输入选项数量"
          />
        </a-form-item>
        <a-form-item field="score" label="每题分数">
          <a-input-number
              min="0"
              max="6"
              v-model="score"
              placeholder="请输入每题分数"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              :loading="submitting"
              type="primary"
              html-type="submit"
              style="width: 120px"
            >
              {{ submitting ? "生成中" : "一键生成" }}
            </a-button>
            <a-button
              :loading="sseSubmitting"
              style="width: 120px"
              @click="handleSSESubmit"
            >
              {{ sseSubmitting ? "生成中" : "实时生成" }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { defineProps, reactive, ref, withDefaults } from "vue";
import API from "@/api";
import {aiGenerateQuestionUsingPost, setQuestionScore, setTrueQuestionNumber} from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import {userLogoutUsingPost} from "@/api/userController";

interface Props {
  appId: string;
  onSuccess?: (result: API.QuestionContentDTO[]) => void;
  onSSESuccess?: (result: API.QuestionContentDTO) => void;
  onSSEStart?: (event: any) => void;
  onSSEClose?: (event: any) => void;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const form = reactive({
  optionNumber: 4,
  questionNumber: 10,
} as API.AiGenerateQuestionRequest);

//每一题的分数设置
const score = ref(10);
//要布置的题目数量
const trueNumber = ref(10);

const visible = ref(false);
const submitting = ref(false);
const sseSubmitting = ref(false);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  //设置分数
  const scoreResult = await setQuestionScore(score.value)
  if (scoreResult.data.code === 0) {
    console.log("设置分数成功！");
  } else {
    message.error("设置分数失败，" + scoreResult.data.message);
  }
  //设置真实题目数
  const trueNumberResult = await setTrueQuestionNumber(trueNumber.value)
  if (trueNumberResult.data.code === 0) {
    console.log("设置要布置的题目数量成功！");
  } else {
    message.error("设置要布置的题目数量失败，" + trueNumberResult.data.message);
  }
  submitting.value = true;
  const res = await aiGenerateQuestionUsingPost({
    appId: props.appId as any,
    ...form,
  });
  if (res.data.code === 0 && res.data.data.length > 0) {
    if (props.onSuccess) {
      props.onSuccess(res.data.data);
    } else {
      message.success("生成题目成功");
    }
    // 关闭抽屉
    handleCancel();
  } else {
    message.error("操作失败，" + res.data.message);
  }
  submitting.value = false;
};

/**
 * 提交（实时生成）
 */
const handleSSESubmit = async () => {
  if (!props.appId) {
    return;
  }
  //设置分数
  const scoreResult = await setQuestionScore(score.value);
  if (scoreResult.data.code === 0) {
    console.log("设置分数成功！");
  } else {
    message.error("设置分数失败，" + scoreResult.data.message);
  }
  //设置真实题目数
  const trueNumberResult = await setTrueQuestionNumber(trueNumber.value)
  if (trueNumberResult.data.code === 0) {
    console.log("设置要布置的题目数量成功！");
  } else {
    message.error("设置要布置的题目数量失败，" + trueNumberResult.data.message);
  }
  sseSubmitting.value = true;
  // 创建 SSE 请求
  const eventSource = new EventSource(
    // todo 手动填写完整的后端地址
    "http://localhost:8101/api/question/ai_generate/sse" +
      `?appId=${props.appId}&optionNumber=${form.optionNumber}&questionNumber=${form.questionNumber}`
  );
  let first = true;
  // 接收消息
  eventSource.onmessage = function (event) {
    if (first) {
      props.onSSEStart?.(event);
      handleCancel();
      first = !first;
    }
    props.onSSESuccess?.(JSON.parse(event.data));
  };
  // 报错或连接关闭时触发
  eventSource.onerror = function (event) {
    if (event.eventPhase === EventSource.CLOSED) {
      console.log("关闭连接");
      props.onSSEClose?.(event);
      eventSource.close();
    } else {
      eventSource.close();
    }
  };
  sseSubmitting.value = false;
};
</script>
