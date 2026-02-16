<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <div class="logo-circle">
            <el-icon :size="50"><Shop /></el-icon>
          </div>
          <h1>乐逸零食店</h1>
          <p>快乐滋味 · 触手可及</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <div class="icon-box"><el-icon><Goods /></el-icon></div>
            <span>精选全球美味零食</span>
          </div>
          <div class="feature-item">
            <div class="icon-box"><el-icon><Stopwatch /></el-icon></div>
            <span>网上下单到店即取</span>
          </div>
          <div class="feature-item">
            <div class="icon-box"><el-icon><Present /></el-icon></div>
            <span>会员专享超值优惠</span>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-card">
          <h2>欢迎回来 👋</h2>
          <p class="login-tip">请输入手机号开启快乐之旅</p>
          
          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="phone">
              <el-input 
                v-model="form.phone" 
                placeholder="请输入手机号"
                size="large"
                maxlength="11"
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="code">
              <div class="code-input">
                <el-input 
                  v-model="form.code" 
                  placeholder="验证码"
                  size="large"
                  maxlength="6"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
                <el-button 
                  size="large"
                  class="code-btn"
                  :disabled="countdown > 0"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            
            <el-button 
              type="primary" 
              size="large" 
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              立即登录
            </el-button>
          </el-form>
          
          <div class="tips">
            <span>未注册手机号将自动创建账号</span>
            <span class="test-code">测试码: 000000</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const formRef = ref()
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  code: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  await formRef.value.validateField('phone')
  try {
    await userStore.sendCode(form.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    console.error(e)
  }
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.phone, form.code)
    ElMessage.success('登录成功')
    await cartStore.fetchCart()
    const redirect = route.query.redirect || '/home'
    router.replace(redirect)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF9FF3 0%, #FECA57 100%);
}

.login-container {
  display: flex;
  width: 960px;
  height: 580px;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-left {
  width: 400px;
  padding: 60px 40px;
  background: linear-gradient(135deg, #FF6B6B 0%, #EE5253 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50px;
    right: -50px;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: rgba(255,255,255,0.1);
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -80px;
    left: -80px;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(255,255,255,0.1);
  }
  
  .brand {
    text-align: center;
    margin-bottom: 60px;
    position: relative;
    z-index: 2;
    
    .logo-circle {
      width: 80px;
      height: 80px;
      background: white;
      border-radius: 50%;
      margin: 0 auto 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #FF6B6B;
      box-shadow: 0 8px 20px rgba(0,0,0,0.2);
    }
    
    h1 {
      font-size: 32px;
      margin: 0 0 10px;
      font-weight: 800;
    }
    
    p {
      opacity: 0.9;
      font-size: 16px;
      letter-spacing: 2px;
    }
  }
  
  .features {
    position: relative;
    z-index: 2;
    
    .feature-item {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
      font-size: 16px;
      font-weight: 500;
      
      .icon-box {
        width: 36px;
        height: 36px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
      }
    }
  }
}

.login-right {
  flex: 1;
  padding: 60px 80px;
  display: flex;
  align-items: center;
  background: white;
  
  .login-card {
    width: 100%;
    
    h2 {
      font-size: 32px;
      margin-bottom: 12px;
      color: #222F3E;
      font-weight: 800;
    }
    
    .login-tip {
      color: #8395A7;
      margin-bottom: 40px;
      font-size: 16px;
    }
  }
  
  .login-form {
    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        background: #F0F3F7;
        box-shadow: none;
        padding: 4px 12px;
        
        &.is-focus {
          background: white;
          box-shadow: 0 0 0 2px #FF6B6B;
        }
      }
    }
  }
  
  .code-input {
    display: flex;
    gap: 16px;
    
    .el-input {
      flex: 1;
    }
    
    .code-btn {
      width: 120px;
      border-radius: 12px;
      background: #FFEAA7;
      border: none;
      color: #E17055;
      font-weight: 600;
      
      &:hover {
        background: #FDCB6E;
      }
      
      &.is-disabled {
        background: #F0F3F7;
        color: #B2BEC3;
      }
    }
  }
  
  .login-btn {
    width: 100%;
    margin-top: 24px;
    height: 50px;
    font-size: 18px;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(255, 107, 107, 0.3);
  }
  
  .tips {
    margin-top: 32px;
    text-align: center;
    color: #8395A7;
    font-size: 14px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .test-code {
      color: #FF6B6B;
      font-weight: 600;
      background: #FFF0F5;
      padding: 4px 12px;
      border-radius: 20px;
      align-self: center;
    }
  }
}
</style>
