<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-left">
        <div class="circles">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
        <div class="brand">
          <el-icon :size="48"><Shop /></el-icon>
          <h1>乐逸后台</h1>
          <p>Leyi Snack Management</p>
        </div>
      </div>
      
      <div class="login-right">
        <div class="form-header">
          <h2>账号登录</h2>
          <p>欢迎回到管理后台</p>
        </div>
        
        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              size="large"
            >
              <template #prefix>
                <el-icon><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
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
        
        <div class="login-footer">
          <div class="account-tag">店长: 13800000001 / 123456</div>
          <div class="account-tag clerk">店员: 13800000002 / 123456</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Iphone, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await adminStore.login(form.phone, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
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
  background: linear-gradient(135deg, #A29BFE 0%, #74B9FF 100%);
  padding: 20px;
}

.login-card {
  width: 800px;
  height: 500px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.2);
  display: flex;
  overflow: hidden;
}

.login-left {
  width: 340px;
  background: #54A0FF;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  
  .circles {
    position: absolute;
    width: 100%;
    height: 100%;
    
    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255,255,255,0.1);
    }
    
    .c1 { width: 200px; height: 200px; top: -50px; left: -50px; }
    .c2 { width: 300px; height: 300px; bottom: -100px; right: -50px; }
    .c3 { width: 100px; height: 100px; top: 100px; right: 20px; }
  }
  
  .brand {
    color: white;
    text-align: center;
    position: relative;
    z-index: 2;
    
    h1 {
      font-size: 28px;
      margin: 16px 0 8px;
      font-weight: 800;
    }
    
    p {
      font-size: 14px;
      opacity: 0.8;
      letter-spacing: 1px;
    }
  }
}

.login-right {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  .form-header {
    margin-bottom: 40px;
    
    h2 {
      font-size: 28px;
      color: #222F3E;
      margin-bottom: 8px;
    }
    
    p {
      color: #8395A7;
    }
  }
  
  .login-form {
    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        background: #F0F3F7;
        box-shadow: none;
        padding: 4px 12px;
        
        &.is-focus {
          background: white;
          box-shadow: 0 0 0 2px #54A0FF;
        }
      }
    }
    
    .login-btn {
      width: 100%;
      margin-top: 24px;
      height: 48px;
      font-size: 16px;
      border-radius: 8px;
      box-shadow: 0 8px 16px rgba(84, 160, 255, 0.3);
    }
  }
  
  .login-footer {
    margin-top: 40px;
    display: flex;
    justify-content: center;
    gap: 12px;
    flex-wrap: wrap;
    
    .account-tag {
      font-size: 12px;
      color: #54A0FF;
      background: rgba(84, 160, 255, 0.1);
      padding: 6px 12px;
      border-radius: 20px;
      font-weight: 600;
      
      &.clerk {
        color: #1DD1A1;
        background: rgba(29, 209, 161, 0.1);
      }
    }
  }
}
</style>
