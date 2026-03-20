// 消息提示助手 - 校园换物平台

/**
 * 消息提示工具类
 */
export class MessageHelper {
  /**
   * 显示成功消息
   * @param {Object} vm - Vue 实例
   * @param {string} message - 消息内容
   */
  static success(vm, message) {
    vm.$message({
      message,
      type: 'success',
      duration: 500,
      customClass: 'glass-message'
    });
  }

  /**
   * 显示警告消息
   * @param {Object} vm - Vue 实例
   * @param {string} message - 消息内容
   */
  static warning(vm, message) {
    vm.$message({
      message,
      type: 'warning',
      duration: 1000,
      customClass: 'glass-message'
    });
  }

  /**
   * 显示错误消息
   * @param {Object} vm - Vue 实例
   * @param {string} message - 消息内容
   */
  static error(vm, message) {
    vm.$message({
      message,
      type: 'error',
      duration: 1500,
      customClass: 'glass-message'
    });
  }

  /**
   * 显示信息消息
   * @param {Object} vm - Vue 实例
   * @param {string} message - 消息内容
   */
  static info(vm, message) {
    vm.$message({
      message,
      type: 'info',
      duration: 500,
      customClass: 'glass-message'
    });
  }
}

/**
 * 确认对话框助手类
 */
export class ConfirmHelper {
  /**
   * 通用确认对话框
   * @param {Object} vm - Vue 实例
   * @param {string} message - 提示消息
   * @param {string} title - 标题
   * @returns {Promise}
   */
  static confirm(vm, message, title = '提示') {
    return vm.$confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
  }

  /**
   * 删除确认
   * @param {Object} vm - Vue 实例
   * @param {string} itemName - 要删除的项目名称
   * @returns {Promise}
   */
  static confirmDelete(vm, itemName) {
    return this.confirm(vm, `此操作将永久删除 ${itemName}, 是否继续?`, '删除确认');
  }
}
