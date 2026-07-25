<template>
  <div class="ai-page">
    <!-- 侧边栏 -->
    <aside class="ai-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-top">
        <div class="logo">
          <el-icon class="logo-icon"><MagicStick /></el-icon>
          <span v-show="!sidebarCollapsed">AI 学习助手</span>
        </div>
        <el-button text class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </el-button>
      </div>

      <el-button
        v-show="!sidebarCollapsed"
        class="new-chat-btn"
        type="primary"
        round
        @click="startNewChat"
      >
        <el-icon><Plus /></el-icon>开启新对话
      </el-button>

      <!-- 历史会话 -->
      <div v-show="!sidebarCollapsed" class="history-list">
        <div class="history-title">历史会话</div>
        <div
          v-for="item in chatHistoryList"
          :key="item.sessionId"
          class="history-item"
          :class="{ active: item.sessionId === sessionId }"
          @click="switchSession(item.sessionId)"
        >
          <el-icon><ChatDotSquare /></el-icon>
          <span class="history-text">{{ item.title || '新对话' }}</span>
        </div>
        <div v-if="!chatHistoryList.length" class="history-empty">暂无历史对话</div>
      </div>

      <!-- 个性化推荐 -->
      <div v-show="!sidebarCollapsed" class="recommend-list">
        <div class="history-title">
          <el-icon><Aim /></el-icon>个性化推荐
        </div>
        <div
          v-for="(r, i) in recommendations"
          :key="i"
          class="recommend-item"
          @click="sendQuickQuestion(r.title || r)"
        >
          <el-icon class="rec-icon"><Star /></el-icon>
          <span>{{ r.title || r }}</span>
        </div>
        <div v-if="!recommendations.length" class="history-empty">暂无推荐</div>
      </div>
    </aside>

    <!-- 主对话区 -->
    <main class="ai-main">
      <!-- 顶部标题 -->
      <header class="ai-header">
        <div class="header-info">
          <h1 class="header-title">
            <el-icon class="title-icon"><MagicStick /></el-icon>
            AI 智能学习助手
          </h1>
          <p class="header-desc">基于 Spring AI 的校园智能问答，解答你的学习疑惑</p>
        </div>
        <div class="header-status">
          <span class="status-dot"></span>
          在线
        </div>
      </header>

      <!-- 对话区域 -->
      <div class="chat-area" ref="chatAreaRef">
        <!-- 欢迎界面 -->
        <div v-if="!messages.length" class="welcome-screen">
          <div class="welcome-icon">
            <el-icon><MagicStick /></el-icon>
          </div>
          <h2>你好，我是 AI 学习助手</h2>
          <p>有问题尽管问我，无论是学习疑难还是复习规划，我都能帮你</p>
          <div class="quick-questions">
            <div class="quick-title">试试这些问题</div>
            <div class="quick-grid">
              <div
                v-for="q in quickQuestions"
                :key="q"
                class="quick-card"
                @click="sendQuickQuestion(q)"
              >
                <el-icon class="quick-icon"><Promotion /></el-icon>
                <span>{{ q }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息气泡 -->
        <template v-else>
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="msg-row"
            :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
          >
            <div class="msg-avatar">
              <el-avatar :size="36" v-if="msg.role === 'user'" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || '我' }}
              </el-avatar>
              <div v-else class="ai-avatar">
                <el-icon><MagicStick /></el-icon>
              </div>
            </div>
            <div class="msg-bubble">
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- AI 思考中动画 -->
          <div v-if="thinking" class="msg-row msg-ai">
            <div class="msg-avatar">
              <div class="ai-avatar">
                <el-icon><MagicStick /></el-icon>
              </div>
            </div>
            <div class="msg-bubble thinking-bubble">
              <div class="thinking-dots">
                <span></span><span></span><span></span>
              </div>
              <div class="thinking-text">AI 思考中...</div>
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区 -->
      <footer class="input-area">
        <div class="input-box">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 5 }"
            placeholder="输入你的问题，按 Enter 发送，Shift+Enter 换行..."
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button
            class="send-btn"
            type="primary"
            round
            :loading="thinking"
            :disabled="!inputText.trim()"
            @click="sendMessage"
          >
            <el-icon><Promotion /></el-icon>
            <span>发送</span>
          </el-button>
        </div>
        <div class="input-tip">AI 生成内容仅供参考，请注意核实重要信息</div>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick, Plus, ChatDotSquare, Aim, Star,
  Promotion, Fold, Expand
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { chatWithAI, getChatHistory, getRecommendations } from '@/api/ai'

const userStore = useUserStore()

const sidebarCollapsed = ref(false)
const inputText = ref('')
const thinking = ref(false)
const chatAreaRef = ref(null)

const sessionId = ref(localStorage.getItem('ai_session_id') || generateSessionId())
const messages = ref([])
const chatHistoryList = ref([])
const recommendations = ref([])

const quickQuestions = [
  '帮我解释快速排序算法',
  '制定一份考研复习计划',
  '如何高效学习英语四六级',
  '推荐几本数据结构入门书'
]

function generateSessionId() {
  const id = 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
  localStorage.setItem('ai_session_id', id)
  return id
}

const formatTime = () => {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatAreaRef.value) {
      chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || thinking.value) return

  messages.value.push({
    role: 'user',
    content: text,
    time: formatTime()
  })
  inputText.value = ''
  scrollToBottom()

  thinking.value = true
  try {
    const data = await chatWithAI({
      message: text,
      sessionId: sessionId.value
    })
    const reply = typeof data === 'string' ? data : (data.content || data.message || data.reply || '抱歉，我暂时无法回答这个问题。')
    messages.value.push({
      role: 'ai',
      content: reply,
      time: formatTime()
    })
  } catch {
    messages.value.push({
      role: 'ai',
      content: '服务开小差了，请稍后再试~',
      time: formatTime()
    })
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}

const sendQuickQuestion = (q) => {
  inputText.value = q
  sendMessage()
}

const startNewChat = () => {
  sessionId.value = generateSessionId()
  messages.value = []
  loadHistory()
}

const switchSession = (sid) => {
  sessionId.value = sid
  localStorage.setItem('ai_session_id', sid)
  messages.value = []
  loadHistory()
}

const loadHistory = async () => {
  try {
    const data = await getChatHistory({ sessionId: sessionId.value })
    if (Array.isArray(data)) {
      messages.value = data.map((m) => ({
        role: m.role || (m.isUser ? 'user' : 'ai'),
        content: m.content,
        time: m.time || formatTime()
      }))
      // 提取会话列表（基于历史中的不同 sessionId）
      if (data.length && data[0].sessions) {
        chatHistoryList.value = data[0].sessions
      }
    }
    scrollToBottom()
  } catch {
    /* 静默 */
  }
}

const loadRecommendations = async () => {
  try {
    const data = await getRecommendations({ userId: userStore.userId })
    recommendations.value = Array.isArray(data) ? data : (data?.list || [])
  } catch {
    recommendations.value = []
  }
}

onMounted(() => {
  loadHistory()
  loadRecommendations()
})
</script>

<style scoped>
.ai-page {
  display: flex;
  height: calc(100vh - 64px);
  background: var(--bg-page);
  overflow: hidden;
}

/* 侧边栏 */
.ai-sidebar {
  width: 260px;
  background: #1e293b;
  display: flex;
  flex-direction: column;
  padding: 16px;
  transition: width 0.3s;
  flex-shrink: 0;
  color: #cbd5e1;
  overflow-y: auto;
}

.ai-sidebar.collapsed {
  width: 64px;
  padding: 16px 8px;
}

.sidebar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}

.logo-icon {
  font-size: 22px;
  color: var(--primary-light);
}

.collapse-btn {
  color: #94a3b8;
}

.new-chat-btn {
  width: 100%;
  margin-bottom: 20px;
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 10px;
  padding: 0 4px;
}

.history-list {
  margin-bottom: 24px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: #cbd5e1;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.history-item:hover {
  background: rgba(99, 102, 241, 0.15);
  color: #fff;
}

.history-item.active {
  background: var(--gradient-primary);
  color: #fff;
}

.history-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-list {
  flex: 1;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: #cbd5e1;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.rec-icon {
  font-size: 14px;
  color: #f59e0b;
}

.recommend-item:hover {
  background: rgba(245, 158, 11, 0.15);
  color: #fff;
}

.history-empty {
  font-size: 12px;
  color: #475569;
  text-align: center;
  padding: 10px;
}

/* 主区域 */
.ai-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 28px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.04), transparent);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.title-icon {
  font-size: 26px;
  color: var(--primary);
}

.header-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--success);
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  animation: pulse-glow 2s infinite;
}

/* 对话区 */
.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 30px 0;
}

/* 欢迎界面 */
.welcome-screen {
  max-width: 720px;
  margin: 0 auto;
  padding: 40px 28px;
  text-align: center;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  box-shadow: 0 12px 30px rgba(99, 102, 241, 0.3);
  animation: pulse-glow 2s infinite;
}

.welcome-screen h2 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.welcome-screen p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 36px;
}

.quick-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-regular);
  text-align: left;
  transition: all 0.25s;
}

.quick-card:hover {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-icon {
  font-size: 18px;
  color: var(--primary);
}

/* 消息气泡 */
.msg-row {
  display: flex;
  gap: 12px;
  max-width: 880px;
  margin: 0 auto 24px;
  padding: 0 28px;
  animation: fadeInUp 0.4s ease;
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.msg-bubble {
  max-width: 75%;
  padding: 14px 18px;
  border-radius: var(--radius-lg);
  position: relative;
}

.msg-user .msg-bubble {
  background: var(--gradient-primary);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.25);
}

.msg-ai .msg-bubble {
  background: var(--bg-card);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  border-top-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

.msg-content {
  font-size: 15px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
}

/* AI 思考动画 */
.thinking-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thinking-dots {
  display: flex;
  gap: 4px;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.thinking-dots span:nth-child(1) { animation-delay: -0.32s; }
.thinking-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.thinking-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 输入区 */
.input-area {
  padding: 16px 28px 20px;
  border-top: 1px solid var(--border-light);
  background: #fff;
}

.input-box {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 880px;
  margin: 0 auto;
  background: var(--bg-page);
  border-radius: 24px;
  padding: 6px 6px 6px 20px;
  border: 1px solid var(--border-light);
  transition: all 0.25s;
}

.input-box:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.input-box :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 8px 0;
  font-size: 15px;
  line-height: 1.6;
}

.send-btn {
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
  padding: 10px 20px;
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.3);
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(99, 102, 241, 0.4);
}

.input-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 10px;
}

@media (max-width: 768px) {
  .ai-sidebar {
    display: none;
  }
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
