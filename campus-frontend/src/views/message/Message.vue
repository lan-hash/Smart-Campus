<template>
  <div class="message-page">
    <div class="container page-wrap">
      <!-- 顶部标题 + 未读 -->
      <div class="msg-header">
        <h1 class="msg-title">
          <el-icon><ChatDotRound /></el-icon>消息中心
        </h1>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <div class="unread-box">
              <el-icon><Bell /></el-icon>
              <span>{{ unreadCount }} 条未读</span>
            </div>
          </el-badge>
        </div>
      </div>

      <!-- Tab 切换 -->
      <div class="tab-bar card-base">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'chat' }"
          @click="switchTab('chat')"
        >
          <el-icon><ChatLineRound /></el-icon>
          <span>私聊</span>
          <span v-if="chatUnread" class="tab-badge">{{ chatUnread }}</span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'notice' }"
          @click="switchTab('notice')"
        >
          <el-icon><Bell /></el-icon>
          <span>通知</span>
          <span v-if="noticeUnread" class="tab-badge">{{ noticeUnread }}</span>
        </div>
      </div>

      <!-- 私聊 -->
      <div v-show="activeTab === 'chat'" class="chat-layout card-base">
        <!-- 会话列表 -->
        <div class="chat-list">
          <div class="list-header">
            <span>会话</span>
            <el-input
              v-model="chatKeyword"
              size="small"
              placeholder="搜索..."
              :prefix-icon="Search"
              clearable
            />
          </div>
          <div class="list-body">
            <div
              v-for="conv in filteredChatList"
              :key="conv.userId"
              class="conv-item"
              :class="{ active: activeChat?.userId === conv.userId }"
              @click="selectChat(conv)"
            >
              <el-badge :value="conv.unread" :hidden="!conv.unread" :max="99">
                <el-avatar :size="44" :src="conv.avatar">
                  {{ conv.nickname?.charAt(0) }}
                </el-avatar>
              </el-badge>
              <div class="conv-info">
                <div class="conv-top">
                  <span class="conv-name">{{ conv.nickname }}</span>
                  <span class="conv-time">{{ formatChatTime(conv.lastTime) }}</span>
                </div>
                <p class="conv-msg">{{ conv.lastMessage || '暂无消息' }}</p>
              </div>
            </div>
            <EmptyState
              v-if="!chatList.length"
              icon="ChatLineRound"
              text="还没有会话"
              sub-text="去逛逛遇到有趣的人吧"
            />
          </div>
        </div>

        <!-- 聊天窗口 -->
        <div class="chat-window">
          <template v-if="activeChat">
            <div class="window-header">
              <el-avatar :size="36" :src="activeChat.avatar">
                {{ activeChat.nickname?.charAt(0) }}
              </el-avatar>
              <div class="window-title">
                <span class="win-name">{{ activeChat.nickname }}</span>
                <span class="win-status">在线</span>
              </div>
            </div>

            <div ref="msgBoxRef" class="window-body" v-loading="msgLoading">
              <div class="msg-list" v-if="msgList.length">
                <div
                  v-for="msg in msgList"
                  :key="msg.id"
                  class="msg-row"
                  :class="{ self: isSelf(msg) }"
                >
                  <el-avatar :size="36" :src="isSelf(msg) ? userStore.userInfo?.avatar : activeChat.avatar">
                    {{ (isSelf(msg) ? userStore.userInfo?.nickname : activeChat.nickname)?.charAt(0) }}
                  </el-avatar>
                  <div class="msg-content">
                    <span class="msg-time">{{ formatChatTime(msg.createTime) }}</span>
                    <div class="msg-bubble">
                      <span v-if="msg.type === 'image'" class="msg-image">
                        <el-image :src="msg.content" fit="cover" :preview-src-list="[msg.content]" class="bubble-img" />
                      </span>
                      <span v-else class="msg-text">{{ msg.content }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <EmptyState
                v-else
                icon="ChatLineSquare"
                text="还没有消息"
                sub-text="打个招呼开始聊天吧"
              />
            </div>

            <div class="window-footer">
              <el-input
                v-model="msgInput"
                placeholder="输入消息，回车发送..."
                :prefix-icon="ChatLineRound"
                @keyup.enter="sendMessage"
              />
              <el-button type="primary" :loading="sending" @click="sendMessage">
                <el-icon><Promotion /></el-icon>发送
              </el-button>
            </div>
          </template>
          <EmptyState
            v-else
            icon="ChatDotRound"
            text="选择一个会话开始聊天"
            sub-text="左侧选择好友开始对话"
          />
        </div>
      </div>

      <!-- 通知 -->
      <div v-show="activeTab === 'notice'" class="notice-wrap card-base">
        <div class="notice-toolbar">
          <div class="notice-filter">
            <el-radio-group v-model="noticeType" @change="fetchNotices">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="like">点赞</el-radio-button>
              <el-radio-button label="comment">评论</el-radio-button>
              <el-radio-button label="follow">关注</el-radio-button>
              <el-radio-button label="system">系统</el-radio-button>
              <el-radio-button label="trade">交易</el-radio-button>
              <el-radio-button label="course">代课</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" plain @click="markAllRead" :disabled="!noticeList.length">
            <el-icon><Check /></el-icon>全部已读
          </el-button>
        </div>

        <div class="notice-list" v-loading="noticeLoading">
          <div
            v-for="item in noticeList"
            :key="item.id"
            class="notice-item"
            :class="{ unread: !item.read }"
            @click="readNotice(item)"
          >
            <div class="notice-icon" :class="`type-${item.type}`">
              <el-icon>
                <component :is="noticeIconMap[item.type] || 'Bell'" />
              </el-icon>
            </div>
            <div class="notice-body">
              <div class="notice-top">
                <span class="notice-title">{{ item.title }}</span>
                <span class="notice-time">{{ formatChatTime(item.createTime) }}</span>
              </div>
              <p class="notice-content">{{ item.content }}</p>
              <div class="notice-meta" v-if="item.link">
                <el-button text size="small" type="primary" @click.stop="goNoticeLink(item)">
                  查看详情<el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
            <span v-if="!item.read" class="unread-dot"></span>
          </div>

          <EmptyState
            v-if="!noticeLoading && !noticeList.length"
            icon="Bell"
            text="暂无通知"
            sub-text="这里会显示你的所有通知消息"
          />
        </div>

        <div v-if="noticeTotal > noticeList.length" class="load-more">
          <el-button text @click="loadMoreNotices">加载更多</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, ChatLineRound, ChatLineSquare, Bell, Search,
  Promotion, Check, ArrowRight, Pointer, Star, User, Setting, Wallet, Document
} from '@element-plus/icons-vue'
import {
  getChatList, getChatMessages, sendChatMessage,
  getNotifications, markRead, markAllRead as markAllReadApi, getUnreadCount
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('chat')

// 未读
const unreadCount = ref(0)
const chatUnread = ref(0)
const noticeUnread = ref(0)

// 私聊
const chatList = ref([])
const chatKeyword = ref('')
const activeChat = ref(null)
const msgList = ref([])
const msgLoading = ref(false)
const msgInput = ref('')
const sending = ref(false)
const msgBoxRef = ref(null)

// 通知
const noticeList = ref([])
const noticeLoading = ref(false)
const noticeType = ref('')
const noticePage = ref(1)
const noticeSize = ref(20)
const noticeTotal = ref(0)

const noticeIconMap = {
  like: Pointer,
  comment: ChatLineRound,
  follow: User,
  system: Setting,
  trade: Wallet,
  course: Document
}

const filteredChatList = computed(() => {
  if (!chatKeyword.value) return chatList.value
  const kw = chatKeyword.value.toLowerCase()
  return chatList.value.filter((c) => c.nickname?.toLowerCase().includes(kw))
})

const formatChatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

const isSelf = (msg) => msg.senderId === userStore.userId || msg.self

// ============ 未读数 ============
const fetchUnread = async () => {
  try {
    const data = await getUnreadCount()
    unreadCount.value = typeof data === 'number' ? data : (data?.total || 0)
    noticeUnread.value = typeof data === 'number' ? data : (data?.notice || 0)
    chatUnread.value = chatList.value.reduce((s, c) => s + (c.unread || 0), 0)
  } catch (e) {
    // ignore
  }
}

// ============ 私聊 ============
const fetchChatList = async () => {
  try {
    const data = await getChatList()
    chatList.value = data || []
    chatUnread.value = chatList.value.reduce((s, c) => s + (c.unread || 0), 0)
    // 默认选中第一个
    if (chatList.value.length && !activeChat.value) {
      selectChat(chatList.value[0])
    }
  } catch (e) {
    // ignore
  }
}

const selectChat = async (conv) => {
  activeChat.value = conv
  msgInput.value = ''
  msgLoading.value = true
  try {
    const data = await getChatMessages(conv.userId, { page: 1, size: 50 })
    msgList.value = data.records || data || []
    // 选中会话后清除该会话未读
    conv.unread = 0
    await nextTick()
    scrollToBottom()
  } catch (e) {
    msgList.value = []
  } finally {
    msgLoading.value = false
  }
}

const sendMessage = async () => {
  if (!msgInput.value.trim() || !activeChat.value) return
  const content = msgInput.value.trim()
  sending.value = true
  // 乐观更新
  const optimistic = {
    id: Date.now(),
    content,
    type: 'text',
    senderId: userStore.userId,
    self: true,
    createTime: new Date().toISOString()
  }
  msgList.value.push(optimistic)
  msgInput.value = ''
  await nextTick()
  scrollToBottom()
  try {
    await sendChatMessage(activeChat.value.userId, { content, type: 'text' })
    // 更新会话最后消息
    activeChat.value.lastMessage = content
    activeChat.value.lastTime = optimistic.createTime
  } catch (e) {
    ElMessage.error('发送失败')
    msgList.value = msgList.value.filter((m) => m.id !== optimistic.id)
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (msgBoxRef.value) {
    msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight
  }
}

// ============ 通知 ============
const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const params = { page: noticePage.value, size: noticeSize.value }
    if (noticeType.value) params.type = noticeType.value
    const data = await getNotifications(params)
    if (noticePage.value === 1) {
      noticeList.value = data.records || []
    } else {
      noticeList.value = [...noticeList.value, ...(data.records || [])]
    }
    noticeTotal.value = data.total || 0
  } catch (e) {
    // ignore
  } finally {
    noticeLoading.value = false
  }
}

const loadMoreNotices = () => {
  noticePage.value += 1
  fetchNotices()
}

const readNotice = async (item) => {
  if (item.read) {
    if (item.link) goNoticeLink(item)
    return
  }
  try {
    await markRead(item.id)
    item.read = true
    noticeUnread.value = Math.max(0, noticeUnread.value - 1)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    if (item.link) goNoticeLink(item)
  } catch (e) {
    // ignore
  }
}

const markAllRead = async () => {
  try {
    await markAllReadApi()
    noticeList.value.forEach((i) => (i.read = true))
    noticeUnread.value = 0
    fetchUnread()
    ElMessage.success('全部已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goNoticeLink = (item) => {
  if (item.link) router.push(item.link)
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'notice' && !noticeList.value.length) {
    fetchNotices()
  }
}

onMounted(() => {
  fetchUnread()
  fetchChatList()
  fetchNotices()
})

// 监听聊天窗口切换时滚动
watch(activeTab, () => {
  if (activeTab.value === 'chat') {
    nextTick(scrollToBottom)
  }
})
</script>

<style scoped>
.message-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-page);
}

/* 顶部 */
.msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.msg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 26px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.unread-box {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-card);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  font-size: 13px;
  color: var(--text-regular);
}

.unread-box .el-icon {
  color: var(--danger);
}

/* Tab */
.tab-bar {
  display: flex;
  gap: 8px;
  padding: 8px;
  margin-bottom: 20px;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 15px;
  color: var(--text-regular);
  transition: all 0.25s;
}

.tab-item:hover {
  background: rgba(99, 102, 241, 0.06);
  color: var(--primary);
}

.tab-item.active {
  background: var(--gradient-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.tab-item.active .tab-badge {
  background: #fff;
  color: var(--primary);
}

/* 私聊布局 */
.chat-layout {
  display: flex;
  height: calc(100vh - 280px);
  min-height: 500px;
  overflow: hidden;
}

.chat-list {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-header > span {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.list-body {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.conv-item:hover {
  background: var(--bg-page);
}

.conv-item.active {
  background: rgba(99, 102, 241, 0.08);
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.conv-time {
  font-size: 11px;
  color: var(--text-secondary);
}

.conv-msg {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 聊天窗口 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.window-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
}

.window-title {
  display: flex;
  flex-direction: column;
}

.win-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.win-status {
  font-size: 12px;
  color: var(--success);
}

.window-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--bg-page);
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.msg-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.msg-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.msg-row.self .msg-content {
  align-items: flex-end;
}

.msg-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--shadow-sm);
  word-break: break-word;
}

.msg-row.self .msg-bubble {
  background: var(--gradient-primary);
  color: #fff;
}

.msg-text {
  font-size: 14px;
  line-height: 1.6;
}

.bubble-img {
  width: 180px;
  height: auto;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.window-footer {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-light);
}

.window-footer .el-input {
  flex: 1;
}

/* 通知 */
.notice-wrap {
  padding: 20px 24px;
}

.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  flex-wrap: wrap;
  gap: 12px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 300px;
}

.notice-item {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  border-radius: var(--radius-md);
  background: var(--bg-page);
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
}

.notice-item:hover {
  background: rgba(99, 102, 241, 0.06);
  transform: translateX(4px);
}

.notice-item.unread {
  background: rgba(99, 102, 241, 0.08);
}

.notice-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.notice-icon.type-like {
  background: linear-gradient(135deg, #ec4899, #f43f5e);
}

.notice-icon.type-comment {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.notice-icon.type-follow {
  background: linear-gradient(135deg, #10b981, #06b6d4);
}

.notice-icon.type-system {
  background: linear-gradient(135deg, #64748b, #475569);
}

.notice-icon.type-trade {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}

.notice-icon.type-course {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
}

.notice-body {
  flex: 1;
  min-width: 0;
}

.notice-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.notice-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.notice-content {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.6;
}

.notice-meta {
  margin-top: 6px;
}

.unread-dot {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger);
  animation: pulse-glow 2s infinite;
}

.load-more {
  text-align: center;
  margin-top: 16px;
}

@media (max-width: 860px) {
  .chat-list {
    width: 220px;
  }
  .msg-content {
    max-width: 80%;
  }
}

@media (max-width: 640px) {
  .chat-layout {
    flex-direction: column;
    height: auto;
  }
  .chat-list {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--border-light);
  }
  .chat-window {
    height: 500px;
  }
  .notice-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
