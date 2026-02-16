<template>
  <div class="success-page">
    <div class="page-container">
      <div class="success-card card">
        <div class="success-icon">
          <el-icon :size="80" color="#34a853"><CircleCheck /></el-icon>
        </div>
        <h1>订单提交成功！</h1>
        <p class="success-tip">请凭取货码到店取货并付款</p>
        
        <div class="verify-code-box">
          <div class="verify-label">取货码</div>
          <div class="verify-code">{{ verifyCode }}</div>
          <el-button type="primary" @click="copyCode">
            <el-icon><CopyDocument /></el-icon>
            复制取货码
          </el-button>
        </div>
        
        <div class="order-info">
          <div class="info-row">
            <span class="label">订单编号</span>
            <span class="value">{{ orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">取货地址</span>
            <span class="value">乐逸零食店 - XX市XX区XX路XX号</span>
          </div>
        </div>
        
        <div class="tips-box">
          <h4><el-icon><InfoFilled /></el-icon> 温馨提示</h4>
          <ul>
            <li>请截图保存取货码，到店后出示给店员</li>
            <li>到店后在收银台完成付款即可取货</li>
            <li>如需取消订单，请在30分钟内操作</li>
          </ul>
        </div>
        
        <div class="action-btns">
          <el-button size="large" @click="router.push('/orders')">查看订单</el-button>
          <el-button type="primary" size="large" @click="router.push('/home')">继续购物</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const orderNo = route.query.orderNo
const verifyCode = route.query.verifyCode

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(verifyCode)
    ElMessage.success('取货码已复制')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style lang="scss" scoped>
.success-page {
  padding: 40px 0;
}

.success-card {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
  padding: 48px;
  
  .success-icon {
    margin-bottom: 24px;
  }
  
  h1 {
    font-size: 28px;
    margin-bottom: 8px;
  }
  
  .success-tip {
    color: #5f6368;
    margin-bottom: 32px;
  }
}

.verify-code-box {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 32px;
  
  .verify-label {
    color: #5f6368;
    margin-bottom: 8px;
  }
  
  .verify-code {
    font-size: 56px;
    font-weight: 700;
    color: #34a853;
    letter-spacing: 8px;
    margin-bottom: 16px;
  }
}

.order-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  text-align: left;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    
    .label {
      color: #5f6368;
    }
  }
}

.tips-box {
  background: #fff8e1;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 32px;
  text-align: left;
  
  h4 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    color: #f9ab00;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    color: #5f6368;
    
    li {
      margin-bottom: 4px;
    }
  }
}

.action-btns {
  display: flex;
  gap: 16px;
  justify-content: center;
  
  .el-button {
    padding: 12px 40px;
  }
}
</style>
