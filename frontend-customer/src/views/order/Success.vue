<template>
  <div class="success-page">
    <div class="page-container success-wrap">
      <section class="success-card leyi-panel-strong">
        <div class="status-icon">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <h1>下单成功</h1>
        <p class="tip">LeYi零食店已收到你的订单，我们会尽快完成备货并推送物流进度。</p>

        <div class="order-code">
          <span>取货码</span>
          <strong>{{ displayVerifyCode }}</strong>
          <el-button type="primary" @click="copyCode">
            <el-icon><CopyDocument /></el-icon>
            复制取货码
          </el-button>
        </div>

        <div class="order-info">
          <p><span>订单编号</span><strong>{{ displayOrderNo }}</strong></p>
          <p><span>门店地址</span><strong>LeYi零食店 - XX市XX区XX路XX号</strong></p>
          <p><span>预计备货</span><strong>1 - 2 小时</strong></p>
        </div>

        <div class="tips-box">
          <h3><el-icon><InfoFilled /></el-icon> 温馨提示</h3>
          <ul>
            <li>建议截图保存本页面，进店后向店员出示取货码。</li>
            <li>若需修改订单，请在 30 分钟内联系在线客服。</li>
            <li>订单动态可在“我的订单”中持续查看。</li>
          </ul>
        </div>

        <div class="action-row">
          <el-button size="large" @click="router.push('/orders')">查看订单</el-button>
          <el-button type="primary" size="large" @click="router.push('/home')">继续购物</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const displayOrderNo = computed(() => route.query.orderNo || '-')
const displayVerifyCode = computed(() => route.query.verifyCode || '待生成')

const copyCode = async () => {
  if (!route.query.verifyCode) {
    ElMessage.warning('当前没有可复制的取货码')
    return
  }

  try {
    await navigator.clipboard.writeText(String(route.query.verifyCode))
    ElMessage.success('取货码已复制')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style lang="scss" scoped>
.success-wrap {
  display: flex;
  justify-content: center;
}

.success-card {
  width: min(100%, 42rem);
  padding: var(--space-12);
  text-align: center;
}

.status-icon {
  width: 5.8rem;
  height: 5.8rem;
  margin: 0 auto;
  border-radius: 50%;
  background: rgba(68, 198, 120, 0.12);
  color: #3f9f67;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-icon .el-icon {
  font-size: 3.2rem;
}

.success-card h1 {
  margin: var(--space-6) 0 var(--space-3);
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.tip {
  margin: 0 auto;
  max-width: 34rem;
  color: var(--color-muted);
  line-height: 1.65;
}

.order-code {
  margin-top: var(--space-8);
  padding: var(--space-8);
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, rgba(238, 205, 43, 0.15), rgba(238, 205, 43, 0.06));
  border: 1px solid rgba(238, 205, 43, 0.38);
}

.order-code span {
  display: block;
  color: var(--color-muted);
  font-size: 0.82rem;
}

.order-code strong {
  display: block;
  margin: var(--space-2) 0 var(--space-5);
  font-size: clamp(2rem, 2.3vw, 2.8rem);
  letter-spacing: 0.08em;
}

.order-info {
  margin-top: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(24, 24, 24, 0.08);
  background: #fcfcf9;
  padding: var(--space-6);
  text-align: left;
}

.order-info p {
  margin: 0 0 var(--space-3);
  display: grid;
  grid-template-columns: 5.5rem minmax(0, 1fr);
  gap: var(--space-4);
  align-items: flex-start;
}

.order-info p:last-child {
  margin-bottom: 0;
}

.order-info span {
  color: var(--color-muted);
  font-size: 0.82rem;
}

.order-info strong {
  font-size: 0.9rem;
  line-height: 1.5;
}

.tips-box {
  margin-top: var(--space-6);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  border: 1px solid rgba(24, 24, 24, 0.08);
  background: #fffdf7;
  text-align: left;
}

.tips-box h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1rem;
}

.tips-box ul {
  margin: var(--space-4) 0 0;
  padding-left: 1.2rem;
  color: var(--color-muted);
}

.tips-box li {
  margin-bottom: var(--space-2);
}

.action-row {
  margin-top: var(--space-8);
  display: flex;
  justify-content: center;
  gap: var(--space-3);
}

.action-row .el-button {
  min-width: 8.5rem;
  height: 2.8rem;
}
</style>
