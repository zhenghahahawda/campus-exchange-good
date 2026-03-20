import { getUserInfo, clearAuth } from '@/utils/auth'

/**
 * 路由中间件：校验用户登录状态
 * - 登录页面不校验
 * - 未登录用户重定向到登录页
 * - 已登录用户验证 token 有效性
 *
 * 安全说明：
 * - 不再依赖 URL 参数进行身份验证（URL 参数可被用户篡改）
 * - 仅从服务端 token 中获取用户身份信息
 * - 如果需要在 URL 中显示用户信息，仅作为展示用途，不作为验证依据
 */
export default function ({ route, redirect }) {
  // 登录页面不校验
  if (route.path.startsWith('/login-page')) {
    return
  }

  // 服务端渲染时跳过
  if (process.server) {
    return
  }

  // 获取用户信息（从 localStorage 的 token 中解析）
  const userInfo = getUserInfo()

  // 未登录用户重定向到登录页
  if (!userInfo || !userInfo.userId) {
    return redirect('/login-page/login')
  }

  // 已登录用户：可选地在 URL 中添加用户信息用于展示
  // 注意：这些参数仅用于 UI 展示，不用于身份验证
  const urlUid = route.query.uid
  const urlUsername = route.query.username
  const urlUserType = route.query.userType

  // 如果 URL 中缺少用户信息参数，自动补全（仅为了 UI 展示）
  if (!urlUid || !urlUsername || !urlUserType) {
    return redirect({
      path: route.path,
      query: {
        ...route.query,
        uid: String(userInfo.userId),
        username: userInfo.username,
        userType: String(userInfo.userType)
      }
    })
  }

  // 如果 URL 参数与实际登录用户不匹配，说明用户可能手动修改了 URL
  // 这种情况下，使用实际登录用户的信息覆盖 URL 参数
  if (
    String(urlUid) !== String(userInfo.userId) ||
    urlUsername !== userInfo.username ||
    String(urlUserType) !== String(userInfo.userType)
  ) {
    // 不清除登录状态，而是纠正 URL 参数
    return redirect({
      path: route.path,
      query: {
        ...route.query,
        uid: String(userInfo.userId),
        username: userInfo.username,
        userType: String(userInfo.userType)
      }
    })
  }
}
