/**
 * 自动在URL中添加用户信息查询参数
 * 登录后所有页面URL都会包含 userId、username、userType
 */

import { getUserInfo } from '@/utils/auth'

export default ({ app }) => {
  // 仅在客户端执行
  if (process.client) {
    // 在路由导航之前添加用户信息
    app.router.beforeEach((to, from, next) => {
      const userInfo = getUserInfo()
      
      // 如果用户已登录且目标路由没有用户信息参数
      if (userInfo && userInfo.userId) {
        const hasUserParams = to.query.userId || to.query.uid
        
        if (!hasUserParams) {
          // 添加用户信息到查询参数
          next({
            path: to.path,
            query: {
              ...to.query,
              userId: userInfo.userId,
              username: userInfo.username,
              userType: userInfo.userType
            },
            params: to.params
          })
          return
        }
      }
      
      next()
    })
  }
}
