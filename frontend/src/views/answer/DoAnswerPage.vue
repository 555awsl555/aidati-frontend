<template>
  <div id="doAnswerPage" style="display: flex">
    <!-- 主内容 -->
    <div style="flex: 3; margin-right: 24px">
      <a-card>
        <h1>{{ app.appName }}</h1>
        <p>{{ app.appDesc }}</p>
        <h2 style="margin-bottom: 16px">
          {{ current }}、{{ currentQuestion?.title }}
        </h2>
        <div>
          <a-radio-group
              direction="vertical"
              v-model="currentAnswer"
              :options="questionOptions"
              @change="doRadioChange"
          />
        </div>
        <div style="margin-top: 24px">
          <a-space size="large">
            <a-button
                type="primary"
                circle
                v-if="current < questionContent.length"
                @click="current += 1"
            >
              下一题
            </a-button>
            <a-button
                type="primary"
                v-if="current === questionContent.length"
                :loading="submitting"
                circle
                :disabled="!allQuestionAnswered"
                @click="doSubmit"
            >
              {{ submitting ? "评分中" : "查看结果" }}
            </a-button>
            <a-button v-if="current > 1" circle @click="current -= 1">
              上一题
            </a-button>
          </a-space>
        </div>
      </a-card>
    </div>

    <!-- 导航栏 -->
    <div style="flex: 1">
      <a-card title="题目导航">
        <div class="navigation">
          <button
              v-for="(answered, index) in questionStatus"
              :key="index"
              :class="['nav-button', { answered }]"
              @click="goToQuestion(index + 1)"
          >
            {{ index + 1 }}
          </button>
        </div>
      </a-card>
    </div>
  </div>
</template>


<script setup lang="ts">
import {
  computed,
  defineProps,
  reactive,
  ref, watch,
  watchEffect,
  withDefaults,
} from "vue";
import API from "@/api";
import { useRouter } from "vue-router";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import { addUserAnswerUsingPost } from "@/api/userAnswerController";

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const router = useRouter();

const app = ref<API.AppVO>({});
// 题目内容结构（理解为题目列表）
const questionContent = ref<API.QuestionContentDTO[]>([]);

// 当前题目的序号（从 1 开始）
const current = ref(1);
// 当前题目
const currentQuestion = ref<API.QuestionContentDTO>({});
// 当前题目选项
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((option) => {
        return {
          label: `${option.key}. ${option.value}`,
          value: option.key ?? "",
        };
      })
    : [];
});
// 当前答案
const currentAnswer = ref<string>();
// 回答列表
const answerList = reactive<string[]>([]);
// 是否正在提交结果
const submitting = ref(false);
// 所有问题都得到回答
var allQuestionAnswered = false;
/**
 * 查看是否所有的问题都得到了回答
 */
const Watch = watch(answerList,(newValue,oldValue)=>{
  if (answerList.length == questionContent.value.length){
    console.log("length true");
    console.log(answerList.slice(0,questionContent.value.length));
    if(judgeQuestionAnswered()){
      console.log("true");
      allQuestionAnswered = true;
    }
  }
})

/**
 * 判断每个题目都答了
 */
const judgeQuestionAnswered = () => {
  for(let i=0;i<questionContent.value.length;i++){
    console.log(i,answerList[i]);
    if(answerList[i]==null||answerList[i]==undefined){
      return false;
    }
  }
  return true;
}
/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.appId) {
    return;
  }
  // 获取 app
  let res: any = await getAppVoByIdUsingGet({
    id: props.appId as any,
  });
  if (res.data.code === 0) {
    app.value = res.data.data as any;
  } else {
    message.error("获取应用失败，" + res.data.message);
  }
  // 获取题目
  res = await listQuestionVoByPageUsingPost({
    appId: props.appId as any,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: "descend",
  });
  if (
    res.data.code === 0 &&
    res.data.data?.records &&
    res.data.data.records[0] != undefined
  ) {
    questionContent.value = res.data.data.records[0].questionContent;
  } else {
    message.error("获取题目失败，" + res.data.message);
  }
};

// 获取旧数据
watchEffect(() => {
  loadData();
});

// 改变 current 题号后，会自动更新当前题目和答案
watchEffect(() => {
  currentQuestion.value = questionContent.value[current.value - 1];
  currentAnswer.value = answerList[current.value - 1];
});

/**
 * 选中选项后，保存选项记录
 * @param value
 */
const doRadioChange = (value: any) => {
  answerList[current.value - 1] = value;
};

/**
 * 提交
 */
const doSubmit = async () => {
  if (!props.appId || !answerList) {
    return;
  }
  submitting.value = true;
  const res = await addUserAnswerUsingPost({
    appId: props.appId as any,
    choices: answerList,
  });
  if (res.data.code === 0 && res.data.data) {
    router.push(`/answer/result/${res.data.data}`);
  } else {
    message.error("提交答案失败，" + res.data.message);
  }
  submitting.value = false;
};

// 动态生成题目状态（已答/未答）
const questionStatus = computed(() =>
    questionContent.value.map((_, index) => !!answerList[index])
);

// 跳转到指定题目
const goToQuestion = (questionNumber: number) => {
  current.value = questionNumber;
};

</script>

<style>
.navigation {
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* 按钮之间的间距 */
}

.nav-button {
  width: 40px;
  height: 40px;
  border: 1px solid #ccc;
  border-radius: 50%;
  background-color: white;
  color: black;
  font-size: 16px;
  cursor: pointer;
}

.nav-button.answered {
  background-color: blue;
  color: white;
}

</style>
