import { getUserInfo } from '@/utils/auth'

/**
 * 路由中间件：管理员权限校验
 * - 仅允许管理员访问后台页面
 * - 普通用户访问后台页面时重定向到前台首页
 */
export default function ({ route, redirect }) {
  // 服务端渲染时跳过
  if (process.server) {
    return
  }

  // 对后台页面和首页进行权限校验
  const protectedPaths = ['/Back-Page', '/index']
  const isRootIndex = route.path === '/'
  const isProtected = isRootIndex || protectedPaths.some(p => route.path.startsWith(p))
  if (!isProtected) {
    return
  }

  // 获取用户信息
  const userInfo = getUserInfo()

  // 未登录用户会被 auth-uid 中间件拦截，这里不需要处理
  if (!userInfo || !userInfo.userId) {
    return
  }

  // 检查是否为管理员
  const isAdmin = userInfo.userType === 1 ||
                  userInfo.userType === '1' ||
                  userInfo.role === 'admin'

  // 非管理员用户重定向到登录页并提示
  if (!isAdmin) {
    return redirect({
      path: '/login-page/login',
      query: {
        error: 'admin_only'
      }
    })
  }
}
