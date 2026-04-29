<script setup lang="ts">
import { ref, onMounted } from 'vue'

const username = ref('')
const password = ref('')
const captchaInput = ref('')
const captchaCode = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)
const canvasRef = ref<HTMLCanvasElement | null>(null)

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
  }
  captchaCode.value = code
  drawCaptcha(code)
}

function drawCaptcha(code: string) {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const w = canvas.width
  const h = canvas.height

  ctx.fillStyle = '#f0f4ff'
  ctx.fillRect(0, 0, w, h)

  // 干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `hsl(${Math.random() * 360}, 60%, 70%)`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * w, Math.random() * h)
    ctx.lineTo(Math.random() * w, Math.random() * h)
    ctx.stroke()
  }

  // 干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `hsl(${Math.random() * 360}, 60%, 70%)`
    ctx.beginPath()
    ctx.arc(Math.random() * w, Math.random() * h, 1, 0, Math.PI * 2)
    ctx.fill()
  }

  // 绘制字符
  const colors = ['#3b5bdb', '#e03131', '#2f9e44', '#e67700', '#7048e8']
  for (let i = 0; i < code.length; i++) {
    ctx.save()
    ctx.font = `bold ${20 + Math.random() * 6}px monospace`
    ctx.fillStyle = colors[i % colors.length]
    ctx.translate(12 + i * 22, h / 2)
    ctx.rotate((Math.random() - 0.5) * 0.5)
    ctx.fillText(code[i], 0, 7)
    ctx.restore()
  }
}

async function handleLogin() {
  errorMsg.value = ''
  successMsg.value = ''

  if (!username.value || !password.value || !captchaInput.value) {
    errorMsg.value = '请填写所有字段'
    return
  }

  if (captchaInput.value.toLowerCase() !== captchaCode.value.toLowerCase()) {
    errorMsg.value = '验证码错误，请重试'
    captchaInput.value = ''
    generateCaptcha()
    return
  }

  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value }),
    })
    const data = await res.json()
    if (res.ok) {
      localStorage.setItem('token', data.token)
      successMsg.value = `登录成功！欢迎回来，${data.username}`
    } else {
      errorMsg.value = data.message || '用户名或密码错误'
      generateCaptcha()
    }
  } catch {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  generateCaptcha()
})
</script>

<template>
  <div class="login-bg">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">🔐</div>
        <h2>欢迎登录</h2>
        <p class="subtitle">请输入您的账号信息</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="field">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </div>

        <div class="field">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>

        <div class="field">
          <label>验证码</label>
          <div class="captcha-row">
            <input
              v-model="captchaInput"
              type="text"
              placeholder="请输入验证码"
              maxlength="4"
              class="captcha-input"
            />
            <canvas
              ref="canvasRef"
              width="110"
              height="40"
              class="captcha-canvas"
              title="点击刷新"
              @click="generateCaptcha"
            />
          </div>
          <span class="refresh-hint" @click="generateCaptcha">看不清？点击刷新</span>
        </div>

        <div v-if="errorMsg" class="msg error">{{ errorMsg }}</div>
        <div v-if="successMsg" class="msg success">{{ successMsg }}</div>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <div class="footer-links">
          <span>测试账号：admin / 123456</span>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  width: 360px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  font-size: 40px;
  margin-bottom: 8px;
}

.login-header h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #1a1a2e;
}

.subtitle {
  margin: 0;
  font-size: 13px;
  color: #888;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}

.field input {
  padding: 10px 14px;
  border: 1.5px solid #dde1f0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  color: #222;
}

.field input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-canvas {
  border-radius: 8px;
  cursor: pointer;
  border: 1.5px solid #dde1f0;
  flex-shrink: 0;
}

.captcha-canvas:hover {
  border-color: #667eea;
}

.refresh-hint {
  font-size: 12px;
  color: #667eea;
  cursor: pointer;
  align-self: flex-start;
}

.refresh-hint:hover {
  text-decoration: underline;
}

.msg {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  text-align: center;
}

.error {
  background: #fff0f0;
  color: #e03131;
  border: 1px solid #ffc9c9;
}

.success {
  background: #ebfbee;
  color: #2f9e44;
  border: 1px solid #b2f2bb;
}

.btn-login {
  padding: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 2px;
  transition: opacity 0.2s, transform 0.1s;
}

.btn-login:hover {
  opacity: 0.92;
}

.btn-login:active {
  transform: scale(0.98);
}

.footer-links {
  text-align: center;
  font-size: 12px;
  color: #aaa;
}
</style>
