<script lang="ts" setup>
import type { BidPortalApi } from '#/api';
import type { Component } from 'vue';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  ArrowDown,
  ArrowRight,
  Calendar,
  Collection,
  DataBoard,
  DocumentChecked,
  Download,
  Files,
  Memo,
  Search,
  Star,
  StarFilled,
  Tickets,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElInput,
  ElMessage,
  ElOption,
  ElPagination,
  ElSelect,
  ElSkeleton,
} from 'element-plus';

import { queryBidPortalProjectPageApi } from '#/api';
import { formatBidDateTime } from '#/utils/bid-helper';

defineOptions({ name: 'PortalProjectList' });

type KpiTone = 'blue' | 'cyan' | 'green' | 'orange';
type TodoTone = 'danger' | 'muted' | 'warning';
type StatusTone = 'blue' | 'green' | 'orange';

interface KpiItem {
  diff: string;
  icon: Component;
  title: string;
  tone: KpiTone;
  trend: 'down' | 'up';
  value: number;
}

interface TodoRow {
  actionText: string;
  currentStep: string;
  deadline: string;
  deadlineLabel: string;
  deadlineTone: TodoTone;
  lotName: string;
  projectName: string;
  remaining: string;
  statusText: string;
  statusTone: StatusTone;
}

interface RecommendedProjectRow {
  bidEndTime: string;
  id: string;
  lotName: string;
  projectId?: number;
  projectName: string;
  region: string;
  registrationEndTime: string;
  tenderUnit: string;
}

interface DeadlineReminder {
  countdown: string;
  deadlineTime: string;
  lotName: string;
  projectName: string;
}

interface ReceiptRow {
  projectName: string;
  receiptNo: string;
  receiptTime: string;
  type: string;
}

const router = useRouter();
const loading = ref(false);
const projectList = ref<BidPortalApi.PortalProjectItem[]>([]);
const total = ref(0);
const activeProjectTab = ref('recommend');
const favoriteRowKeys = ref<string[]>(['demo-industrial-park']);

const queryForm = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 5,
  searchCount: true,
});

const filters = reactive({
  industry: '',
  projectType: '',
  region: '',
});

const projectTabs = [
  { label: '推荐项目', value: 'recommend' },
  { label: '最新项目', value: 'latest' },
  { label: '更多项目', value: 'more' },
];

const kpiItems: KpiItem[] = [
  {
    diff: '+6',
    icon: DocumentChecked,
    title: '可参与项目',
    tone: 'blue',
    trend: 'up',
    value: 68,
  },
  {
    diff: '+2',
    icon: Memo,
    title: '待提交投标',
    tone: 'orange',
    trend: 'up',
    value: 12,
  },
  {
    diff: '-1',
    icon: Calendar,
    title: '今日截止',
    tone: 'green',
    trend: 'down',
    value: 5,
  },
  {
    diff: '+3',
    icon: DataBoard,
    title: '已出结果',
    tone: 'cyan',
    trend: 'up',
    value: 23,
  },
];

const todoRows: TodoRow[] = [
  {
    actionText: '继续办理',
    currentStep: '投标文件编制',
    deadline: '2025-05-21 17:00',
    deadlineLabel: '今天截止',
    deadlineTone: 'danger',
    lotName: '土建施工一标段',
    projectName: '北京市轨道交通12号线工程土建施工项目',
    remaining: '02:45:36',
    statusText: '待提交投标',
    statusTone: 'orange',
  },
  {
    actionText: '查看进度',
    currentStep: '报名审核中',
    deadline: '2025-05-24 17:00',
    deadlineLabel: '3天后截止',
    deadlineTone: 'warning',
    lotName: '设计施工总承包标段',
    projectName: '北京某产业园区基础设施建设项目',
    remaining: '3天 02:45',
    statusText: '已报名',
    statusTone: 'green',
  },
  {
    actionText: '查看招标文件',
    currentStep: '招标文件获取',
    deadline: '2025-05-26 09:30',
    deadlineLabel: '5天后截止',
    deadlineTone: 'muted',
    lotName: '施工标段',
    projectName: '北京某学校改扩建工程项目',
    remaining: '5天 19:15',
    statusText: '未报名',
    statusTone: 'blue',
  },
];

const fallbackProjectRows: RecommendedProjectRow[] = [
  {
    bidEndTime: '2025-06-03 09:30',
    id: 'demo-city-center',
    lotName: '综合管廊工程施工一标段',
    projectName: '北京市城市副中心综合管廊建设项目',
    region: '北京 通州',
    registrationEndTime: '2025-05-27 17:00',
    tenderUnit: '北京市基础设施投资有限公司',
  },
  {
    bidEndTime: '2025-05-31 14:00',
    id: 'demo-industrial-park',
    lotName: '设计施工总承包标段',
    projectName: '北京某产业园区基础设施建设项目',
    region: '北京 大兴',
    registrationEndTime: '2025-05-24 17:00',
    tenderUnit: '北京经济技术开发区管理委员会',
  },
  {
    bidEndTime: '2025-06-04 09:30',
    id: 'demo-jingjin',
    lotName: '机电安装工程标段',
    projectName: '京津冀协同发展交通枢纽工程',
    region: '河北 廊坊',
    registrationEndTime: '2025-05-28 17:00',
    tenderUnit: '河北交通投资集团有限公司',
  },
  {
    bidEndTime: '2025-06-06 10:00',
    id: 'demo-rail-12',
    lotName: '土建施工二标段',
    projectName: '北京市轨道交通12号线工程土建施工项目',
    region: '北京 海淀',
    registrationEndTime: '2025-05-29 17:00',
    tenderUnit: '北京市基础设施投资有限公司',
  },
  {
    bidEndTime: '2025-06-09 14:00',
    id: 'demo-smart-park',
    lotName: '智能化工程标段',
    projectName: '某智慧园区配套工程项目',
    region: '北京 昌平',
    registrationEndTime: '2025-06-01 17:00',
    tenderUnit: '北京某科技产业发展有限公司',
  },
];

const deadlineReminders: DeadlineReminder[] = [
  {
    countdown: '02:45:36',
    deadlineTime: '17:00',
    lotName: '土建施工一标段',
    projectName: '北京市轨道交通12号线工程土建施工项目',
  },
  {
    countdown: '03:15:36',
    deadlineTime: '17:30',
    lotName: '机电安装工程标段',
    projectName: '某医院改扩建机电工程项目',
  },
  {
    countdown: '03:45:36',
    deadlineTime: '18:00',
    lotName: '机电安装工程标段',
    projectName: '某数据中心建设项目',
  },
  {
    countdown: '04:15:36',
    deadlineTime: '18:30',
    lotName: '施工标段',
    projectName: '北京某学校改扩建工程项目',
  },
  {
    countdown: '04:45:36',
    deadlineTime: '19:00',
    lotName: '幕墙工程标段',
    projectName: '某商业综合体幕墙工程项目',
  },
];

const qualificationRows = [
  { label: '认证状态', value: '已认证', valueClass: 'is-success' },
  { label: '信用等级', value: 'AA', valueClass: 'is-success' },
  { label: '证件有效期', value: '2026-04-30' },
  { label: '可投标范围', value: '建筑工程施工总承包一级 等 3 项', hasArrow: true },
];

const qualificationActions = [
  { icon: Tickets, label: '资质管理' },
  { icon: Files, label: '信息维护' },
  { icon: Collection, label: '信用报告' },
];

const receiptRows: ReceiptRow[] = [
  {
    projectName: '轨道交通12号线...',
    receiptNo: 'HS202505200001',
    receiptTime: '2025-05-20 16:08',
    type: '投标文件',
  },
  {
    projectName: '产业园区基础设...',
    receiptNo: 'BM202505190045',
    receiptTime: '2025-05-19 11:23',
    type: '报名',
  },
  {
    projectName: '学校改扩建工程...',
    receiptNo: 'HS202505180033',
    receiptTime: '2025-05-18 09:15',
    type: '投标文件',
  },
  {
    projectName: '智慧园区配套工...',
    receiptNo: 'BM202505170018',
    receiptTime: '2025-05-17 14:32',
    type: '报名',
  },
  {
    projectName: '数据中心建设项...',
    receiptNo: 'HS202505160022',
    receiptTime: '2025-05-16 10:05',
    type: '投标文件',
  },
];

const dashboardTotal = computed(() => Math.max(total.value, 68));

const apiRecommendedRows = computed<RecommendedProjectRow[]>(() =>
  projectList.value.slice(0, 5).map((project, index) => {
    const lot = project.lots?.[0];
    const fallbackRow = fallbackProjectRows[index] ?? fallbackProjectRows[0]!;
    return {
      bidEndTime: toDisplayDate(lot?.bidEndTime, fallbackRow.bidEndTime),
      id: `project-${project.projectId}-${lot?.lotId ?? 'main'}`,
      lotName: lot?.lotName || fallbackRow.lotName,
      projectId: project.projectId,
      projectName: project.projectName,
      region: fallbackRow.region,
      registrationEndTime: toDisplayDate(
        lot?.registrationEndTime,
        fallbackRow.registrationEndTime,
      ),
      tenderUnit: fallbackRow.tenderUnit,
    };
  }),
);

const recommendedRows = computed(() => {
  const seen = new Set<string>();
  return [...apiRecommendedRows.value, ...fallbackProjectRows]
    .filter((row) => {
      const key = `${row.projectName}-${row.lotName}`;
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    })
    .slice(0, 5);
});

function toDisplayDate(value: string | undefined, fallback: string) {
  const formatted = formatBidDateTime(value);
  return formatted === '--' ? fallback : formatted;
}

async function loadData() {
  loading.value = true;
  try {
    const result = await queryBidPortalProjectPageApi(queryForm);
    projectList.value = result.list ?? [];
    total.value = result.total ?? 0;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  queryForm.keyword = '';
  queryForm.pageNum = 1;
  filters.industry = '';
  filters.projectType = '';
  filters.region = '';
  void loadData();
}

function handlePageChange(pageNum: number) {
  queryForm.pageNum = pageNum;
  void loadData();
}

function handleSizeChange(pageSize: number) {
  queryForm.pageNum = 1;
  queryForm.pageSize = pageSize;
  void loadData();
}

function openProjectFile(row: RecommendedProjectRow) {
  if (row.projectId) {
    router.push({
      path: '/bid-portal/project/detail',
      query: { projectId: String(row.projectId) },
    });
    return;
  }
  ElMessage.info('演示项目暂未关联招标文件详情');
}

function handleTodoAction(row: TodoRow) {
  ElMessage.success(`已进入「${row.actionText}」流程`);
}

function downloadTenderFile(row: RecommendedProjectRow) {
  ElMessage.success(`已开始下载：${row.projectName}`);
}

function rowKey(row: RecommendedProjectRow) {
  return row.id;
}

function isFavorite(row: RecommendedProjectRow) {
  return favoriteRowKeys.value.includes(rowKey(row));
}

function toggleFavorite(row: RecommendedProjectRow) {
  const key = rowKey(row);
  favoriteRowKeys.value = favoriteRowKeys.value.includes(key)
    ? favoriteRowKeys.value.filter((item) => item !== key)
    : [...favoriteRowKeys.value, key];
}

onMounted(() => {
  void loadData();
});
</script>

<template>
  <section class="portal-page portal-workbench-page">
    <div class="portal-workbench-layout">
      <main class="portal-workbench-left">
        <section class="workbench-card kpi-card" aria-label="项目概览">
          <div
            v-for="item in kpiItems"
            :key="item.title"
            class="kpi-card__item"
          >
            <span :class="['kpi-card__icon', `is-${item.tone}`]">
              <component :is="item.icon" />
            </span>
            <div class="kpi-card__copy">
              <span>{{ item.title }}</span>
              <strong>{{ item.value }}</strong>
              <small>
                较昨日
                <b :class="item.trend === 'up' ? 'is-up' : 'is-down'">
                  {{ item.diff }}
                </b>
              </small>
            </div>
          </div>
        </section>

        <section class="workbench-card todo-card">
          <div class="workbench-card__title">
            <h2>待办理（3）</h2>
          </div>
          <div class="todo-table">
            <div class="todo-table__row todo-table__head">
              <span></span>
              <span>项目名称 / 标段名称</span>
              <span>当前环节</span>
              <span>截止时间</span>
              <span>剩余时间</span>
              <span>状态</span>
              <span>操作</span>
            </div>
            <div
              v-for="row in todoRows"
              :key="`${row.projectName}-${row.lotName}`"
              class="todo-table__row"
            >
              <span :class="['deadline-badge', `is-${row.deadlineTone}`]">
                {{ row.deadlineLabel }}
              </span>
              <div class="table-name-cell">
                <button type="button" @click="handleTodoAction(row)">
                  {{ row.projectName }}
                </button>
                <small>{{ row.lotName }}</small>
              </div>
              <span>{{ row.currentStep }}</span>
              <span>{{ row.deadline }}</span>
              <strong class="remaining-time">{{ row.remaining }}</strong>
              <span :class="['status-pill', `is-${row.statusTone}`]">
                {{ row.statusText }}
              </span>
              <div class="todo-table__actions">
                <ElButton
                  :plain="row.statusTone !== 'orange'"
                  size="small"
                  :type="row.statusTone === 'orange' ? 'primary' : 'primary'"
                  @click="handleTodoAction(row)"
                >
                  {{ row.actionText }}
                </ElButton>
                <ElButton :icon="ArrowDown" size="small" />
              </div>
            </div>
          </div>
        </section>

        <section class="workbench-card project-card">
          <div class="project-card__tabs">
            <button
              v-for="tab in projectTabs"
              :key="tab.value"
              :class="{ 'is-active': activeProjectTab === tab.value }"
              type="button"
              @click="activeProjectTab = tab.value"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="project-card__filters">
            <ElInput
              v-model="queryForm.keyword"
              :prefix-icon="Search"
              clearable
              placeholder="请输入项目名称或标段名称"
              @keyup.enter="loadData"
            />
            <label>
              <span>地区</span>
              <ElSelect v-model="filters.region" placeholder="全部">
                <ElOption label="全部" value="" />
                <ElOption label="北京" value="北京" />
                <ElOption label="河北" value="河北" />
              </ElSelect>
            </label>
            <label>
              <span>行业</span>
              <ElSelect v-model="filters.industry" placeholder="全部">
                <ElOption label="全部" value="" />
                <ElOption label="工程建设" value="工程建设" />
                <ElOption label="轨道交通" value="轨道交通" />
              </ElSelect>
            </label>
            <label>
              <span>项目类型</span>
              <ElSelect v-model="filters.projectType" placeholder="全部">
                <ElOption label="全部" value="" />
                <ElOption label="工程" value="工程" />
                <ElOption label="货物" value="货物" />
                <ElOption label="服务" value="服务" />
              </ElSelect>
            </label>
            <ElButton @click="resetQuery">重置</ElButton>
          </div>

          <ElSkeleton :loading="loading" animated :rows="6">
            <template #default>
              <div class="recommend-table">
                <div class="recommend-table__row recommend-table__head">
                  <span>项目名称 / 标段名称</span>
                  <span>招标单位</span>
                  <span>报名截止时间</span>
                  <span>投标截止时间</span>
                  <span>地区</span>
                  <span>操作</span>
                </div>
                <div
                  v-for="row in recommendedRows"
                  :key="row.id"
                  class="recommend-table__row"
                >
                  <div class="table-name-cell">
                    <button type="button" @click="openProjectFile(row)">
                      {{ row.projectName }}
                    </button>
                    <small>{{ row.lotName }}</small>
                  </div>
                  <span>{{ row.tenderUnit }}</span>
                  <span>{{ row.registrationEndTime }}</span>
                  <span>{{ row.bidEndTime }}</span>
                  <span>{{ row.region }}</span>
                  <div class="recommend-table__actions">
                    <button type="button" @click="openProjectFile(row)">
                      查看招标文件
                    </button>
                    <button
                      class="icon-action"
                      type="button"
                      aria-label="下载招标文件"
                      @click="downloadTenderFile(row)"
                    >
                      <Download />
                    </button>
                    <button
                      class="icon-action is-muted"
                      type="button"
                      aria-label="收藏项目"
                      @click="toggleFavorite(row)"
                    >
                      <component :is="isFavorite(row) ? StarFilled : Star" />
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </ElSkeleton>

          <div class="project-card__footer">
            <ElPagination
              v-model:current-page="queryForm.pageNum"
              v-model:page-size="queryForm.pageSize"
              :page-sizes="[5, 10, 20]"
              :total="dashboardTotal"
              layout="total, prev, pager, next, sizes, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </section>
      </main>

      <aside class="portal-workbench-right">
        <section class="workbench-card side-card">
          <div class="side-card__title">
            <h2>今日截止提醒</h2>
            <button type="button">更多 <ArrowRight /></button>
          </div>
          <div class="deadline-list">
            <div
              v-for="item in deadlineReminders"
              :key="`${item.projectName}-${item.deadlineTime}`"
              class="deadline-list__item"
            >
              <div>
                <strong>{{ item.projectName }}</strong>
                <small>{{ item.lotName }}</small>
              </div>
              <div>
                <span>{{ item.deadlineTime }}</span>
                <b>{{ item.countdown }}</b>
              </div>
            </div>
          </div>
        </section>

        <section class="workbench-card side-card qualification-card">
          <div class="side-card__title">
            <h2>企业资质与信息</h2>
            <button type="button">更多 <ArrowRight /></button>
          </div>
          <dl class="qualification-list">
            <div
              v-for="item in qualificationRows"
              :key="item.label"
              class="qualification-list__row"
            >
              <dt>{{ item.label }}</dt>
              <dd :class="item.valueClass">
                {{ item.value }}
                <ArrowRight v-if="item.hasArrow" />
              </dd>
            </div>
          </dl>
          <div class="qualification-actions">
            <ElButton
              v-for="action in qualificationActions"
              :key="action.label"
              :icon="action.icon"
            >
              {{ action.label }}
            </ElButton>
          </div>
        </section>

        <section class="workbench-card side-card receipt-card">
          <div class="side-card__title">
            <h2>最近回执</h2>
            <button type="button">更多 <ArrowRight /></button>
          </div>
          <div class="receipt-table">
            <div class="receipt-table__row receipt-table__head">
              <span>回执号</span>
              <span>项目名称</span>
              <span>回执时间</span>
              <span>类型</span>
            </div>
            <div
              v-for="row in receiptRows"
              :key="row.receiptNo"
              class="receipt-table__row"
            >
              <span>{{ row.receiptNo }}</span>
              <span>{{ row.projectName }}</span>
              <span>{{ row.receiptTime }}</span>
              <span>{{ row.type }}</span>
            </div>
          </div>
          <p class="receipt-card__note">
            说明：回执可在「我的投标-回执管理」中查看和下载。
          </p>
        </section>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.portal-workbench-page {
  min-height: calc(100vh - var(--portal-header-height));
  padding: 16px 18px 18px;
  background: #f5f8fc;
}

.portal-workbench-layout {
  display: grid;
  grid-template-columns: minmax(760px, 1fr) 414px;
  gap: 14px;
  max-width: 1488px;
  margin: 0 auto;
}

.portal-workbench-left,
.portal-workbench-right {
  display: grid;
  align-content: start;
  gap: 14px;
  min-width: 0;
}

.workbench-card {
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5eaf2;
  border-radius: 5px;
  box-shadow: 0 2px 8px rgb(17 24 39 / 3%);
}

.workbench-card__title,
.side-card__title,
.project-card__tabs {
  display: flex;
  align-items: center;
}

.workbench-card__title {
  height: 48px;
  padding: 0 18px;
}

.workbench-card__title h2,
.side-card__title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.kpi-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  height: 126px;
  padding: 28px 22px;
}

.kpi-card__item {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 18px;
  align-items: center;
  min-width: 0;
  padding: 0 22px;
  border-right: 1px solid #dfe6f0;
}

.kpi-card__item:first-child {
  padding-left: 0;
}

.kpi-card__item:last-child {
  border-right: 0;
}

.kpi-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgb(22 119 255 / 14%);
}

.kpi-card__icon svg {
  width: 27px;
  height: 27px;
}

.kpi-card__icon.is-blue {
  background: linear-gradient(135deg, #2f7cff, #4465f2);
}

.kpi-card__icon.is-orange {
  background: linear-gradient(135deg, #ff9f3f, #ff7d1a);
}

.kpi-card__icon.is-green {
  background: linear-gradient(135deg, #23c760, #12a848);
}

.kpi-card__icon.is-cyan {
  background: linear-gradient(135deg, #42b8f7, #2699e8);
}

.kpi-card__copy {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.kpi-card__copy span,
.kpi-card__copy small {
  color: #6b7280;
}

.kpi-card__copy strong {
  font-size: 30px;
  font-weight: 500;
  line-height: 1.08;
  color: #111827;
}

.kpi-card__copy strong::after {
  margin-left: 6px;
  font-size: 22px;
  color: #4b5563;
  content: "↑";
}

.kpi-card__copy b {
  margin-left: 6px;
  font-weight: 600;
}

.kpi-card__copy .is-up {
  color: #16a34a;
}

.kpi-card__copy .is-down {
  color: #16a34a;
}

.todo-card {
  min-height: 292px;
}

.todo-table,
.recommend-table,
.receipt-table {
  min-width: 0;
}

.todo-table__row,
.recommend-table__row,
.receipt-table__row {
  display: grid;
  align-items: center;
  border-top: 1px solid #e5eaf2;
}

.todo-table__row {
  grid-template-columns: 78px minmax(220px, 1.9fr) 124px 148px 112px 112px 144px;
  min-height: 61px;
  padding: 0 18px;
  column-gap: 12px;
}

.todo-table__head,
.recommend-table__head,
.receipt-table__head {
  min-height: 42px;
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  background: #fbfcfe;
}

.table-name-cell {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.table-name-cell button {
  width: fit-content;
  max-width: 100%;
  padding: 0;
  overflow: hidden;
  font-weight: 600;
  color: #1677ff;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  background: transparent;
  border: 0;
}

.table-name-cell small {
  overflow: hidden;
  color: #6b7280;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deadline-badge,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  min-width: 66px;
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
}

.deadline-badge.is-danger {
  color: #f5222d;
  background: #fff1f0;
}

.deadline-badge.is-warning {
  color: #fa8c16;
  background: #fff7e6;
}

.deadline-badge.is-muted {
  color: #6b7280;
  background: #f2f4f7;
}

.remaining-time {
  font-weight: 500;
  color: #f5222d;
}

.status-pill.is-orange {
  color: #fa8c16;
  background: #fff7e6;
}

.status-pill.is-green {
  color: #16a34a;
  background: #edf9f1;
}

.status-pill.is-blue {
  color: #1677ff;
  background: #eef5ff;
}

.todo-table__actions,
.recommend-table__actions,
.qualification-actions {
  display: flex;
  align-items: center;
}

.todo-table__actions {
  gap: 6px;
}

.todo-table__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.project-card__tabs {
  height: 45px;
  padding: 0 18px;
  border-bottom: 1px solid #e5eaf2;
}

.project-card__tabs button {
  position: relative;
  height: 45px;
  padding: 0 2px;
  margin-right: 32px;
  font-size: 16px;
  font-weight: 600;
  color: #4b5563;
  cursor: pointer;
  background: transparent;
  border: 0;
}

.project-card__tabs button::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 2px;
  background: transparent;
  content: "";
}

.project-card__tabs button.is-active {
  color: #1677ff;
}

.project-card__tabs button.is-active::after {
  background: #1677ff;
}

.project-card__filters {
  display: grid;
  grid-template-columns: 260px 132px 132px 154px 56px;
  gap: 20px;
  align-items: center;
  height: 73px;
  padding: 0 18px;
  border-bottom: 1px solid #e5eaf2;
}

.project-card__filters label {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 9px;
  align-items: center;
  color: #6b7280;
}

.project-card__filters :deep(.el-input__wrapper),
.project-card__filters :deep(.el-select__wrapper) {
  min-height: 32px;
  border-radius: 4px;
}

.recommend-table__row {
  grid-template-columns: minmax(240px, 1.8fr) minmax(170px, 1.1fr) 136px 136px 88px 166px;
  min-height: 55px;
  padding: 0 18px;
  column-gap: 14px;
}

.recommend-table__row > span {
  overflow: hidden;
  color: #374151;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-table__actions {
  gap: 12px;
  justify-content: flex-end;
}

.recommend-table__actions > button {
  padding: 0;
  font-weight: 600;
  color: #1677ff;
  cursor: pointer;
  background: transparent;
  border: 0;
}

.icon-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.icon-action svg {
  width: 17px;
  height: 17px;
}

.icon-action.is-muted {
  color: #8b95a1;
}

.project-card__footer {
  display: flex;
  justify-content: space-between;
  min-height: 56px;
  padding: 9px 18px 12px;
}

.project-card__footer :deep(.el-pagination) {
  width: 100%;
}

.project-card__footer :deep(.el-pagination__sizes) {
  margin-left: auto;
}

.side-card {
  min-height: 0;
}

.side-card__title {
  position: relative;
  justify-content: space-between;
  height: 50px;
  padding: 0 14px 0 28px;
  border-bottom: 1px solid #e5eaf2;
}

.side-card__title::before {
  position: absolute;
  width: 4px;
  height: 18px;
  margin-left: -16px;
  background: #1677ff;
  border-radius: 2px;
  content: "";
}

.side-card__title button {
  display: inline-flex;
  gap: 3px;
  align-items: center;
  padding: 0;
  color: #4b5563;
  cursor: pointer;
  background: transparent;
  border: 0;
}

.side-card__title svg {
  width: 14px;
  height: 14px;
}

.deadline-list__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 74px;
  gap: 12px;
  min-height: 62px;
  padding: 9px 24px;
  border-bottom: 1px solid #eef2f7;
}

.deadline-list__item:last-child {
  border-bottom: 0;
}

.deadline-list__item div {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.deadline-list__item strong,
.deadline-list__item small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deadline-list__item strong {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.deadline-list__item small,
.deadline-list__item span {
  color: #6b7280;
}

.deadline-list__item div:last-child {
  justify-items: end;
}

.deadline-list__item b {
  font-weight: 500;
  color: #f5222d;
}

.qualification-card {
  min-height: 226px;
}

.qualification-list {
  display: grid;
  gap: 11px;
  margin: 18px 28px 16px;
}

.qualification-list__row {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 14px;
  align-items: start;
}

.qualification-list__row dt {
  color: #1f2937;
}

.qualification-list__row dd {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  margin: 0;
  color: #111827;
}

.qualification-list__row dd.is-success {
  font-weight: 600;
  color: #16a34a;
}

.qualification-list__row svg {
  width: 13px;
  height: 13px;
  color: #6b7280;
}

.qualification-actions {
  gap: 10px;
  padding: 0 14px 12px;
}

.qualification-actions :deep(.el-button) {
  flex: 1;
  height: 34px;
  margin-left: 0;
  color: #3b82f6;
  border-color: #dbe6f5;
}

.receipt-card {
  min-height: 323px;
}

.receipt-table {
  padding: 10px 14px 0;
}

.receipt-table__row {
  grid-template-columns: 112px minmax(0, 1fr) 106px 52px;
  min-height: 33px;
  padding: 0;
  column-gap: 8px;
  border-top: 0;
}

.receipt-table__head {
  min-height: 32px;
  background: transparent;
}

.receipt-table__row span {
  overflow: hidden;
  font-size: 12px;
  color: #374151;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.receipt-card__note {
  margin: 12px 14px 0;
  font-size: 12px;
  color: #8b95a1;
}

@media (max-width: 1180px) {
  .portal-workbench-layout {
    grid-template-columns: 1fr;
  }

  .portal-workbench-right {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .kpi-card,
  .portal-workbench-right {
    grid-template-columns: 1fr;
  }

  .kpi-card {
    height: auto;
    padding: 12px;
  }

  .kpi-card__item {
    padding: 12px 0;
    border-right: 0;
    border-bottom: 1px solid #e5eaf2;
  }

  .todo-table,
  .recommend-table {
    overflow-x: auto;
  }

  .todo-table__row {
    min-width: 900px;
  }

  .recommend-table__row {
    min-width: 860px;
  }

  .project-card__filters {
    grid-template-columns: 1fr;
    height: auto;
    padding: 14px 18px;
  }
}
</style>
