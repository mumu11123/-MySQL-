import axios from 'axios'

const http = axios.create({
  baseURL: '',
  timeout: 8000
})

// 请求拦截：自动携带Token（优先商家token，其次管理员token）
http.interceptors.request.use(config => {
  const merchantToken = localStorage.getItem('merchantToken')
  const adminToken = localStorage.getItem('adminToken')
  const token = merchantToken || adminToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截
http.interceptors.response.use(
  res => res.data,
  err => {
    const isLoginApi = err.config?.url?.includes('/login/merchant')

    // 登录接口：400 是账号密码错误或参数校验失败，正常返回数据
    if (isLoginApi && err.response?.status === 400) {
      return Promise.resolve(err.response.data)
    }

    // 其他接口的401未登录，跳转登录页
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('merchantToken')
      location.href = '/login'
    }

    return Promise.reject(err)
  }
)

export default http
