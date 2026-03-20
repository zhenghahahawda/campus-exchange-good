import { MessageHelper } from '@/utils/messageHelper'

/**
 * 判断 API 响应是否成功
 */
export function isSuccess(res) {
  return res && (res.code === 200 || res.code === 20000 || res.code === 0)
}

/**
 * 执行带确认的 API 操作（删除、审核等）
 * @param {Object} vm - Vue 组件实例
 * @param {Function} confirmFn - 确认函数，返回 Promise
 * @param {Function} apiFn - API 调用函数，返回 Promise
 * @param {string} successMsg - 成功提示
 * @param {string} failMsg - 失败提示
 * @param {Function} onSuccess - 成功回调
 * @returns {Promise<boolean>}
 */
export async function confirmAndExecute(vm, { confirmFn, apiFn, successMsg, failMsg = '操作失败', onSuccess }) {
  try {
    if (confirmFn) await confirmFn()

    const res = await apiFn()
    if (isSuccess(res)) {
      if (successMsg) MessageHelper.success(vm, successMsg)
      if (onSuccess) await onSuccess(res)
      return true
    } else {
      MessageHelper.error(vm, res?.message || failMsg)
      return false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      MessageHelper.error(vm, failMsg)
    }
    return false
  }
}

/**
 * 执行带 loading 状态的 API 请求
 * @param {Object} vm - Vue 组件实例
 * @param {string} loadingKey - loading 状态的 key
 * @param {Function} apiFn - API 调用函数
 * @param {string} failMsg - 失败提示
 * @returns {Promise<any>} 响应数据
 */
export async function withLoading(vm, loadingKey, apiFn, failMsg = '加载失败') {
  try {
    vm[loadingKey] = true
    const res = await apiFn()
    if (isSuccess(res)) {
      return res.data
    } else {
      MessageHelper.error(vm, res?.message || failMsg)
      return null
    }
  } catch (error) {
    console.error(error)
    MessageHelper.error(vm, failMsg)
    return null
  } finally {
    vm[loadingKey] = false
  }
}
