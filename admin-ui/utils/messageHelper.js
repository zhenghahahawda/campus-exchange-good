import { MESSAGE_TYPES } from './constants';

/**
 * 消息提示助手类
 */
export class MessageHelper {
  static success(vm, message) {
    vm.$root.$emit('show-island-message', message, MESSAGE_TYPES.SUCCESS);
  }

  static warning(vm, message) {
    vm.$root.$emit('show-island-message', message, MESSAGE_TYPES.WARNING);
  }

  static error(vm, message) {
    vm.$root.$emit('show-island-message', message, MESSAGE_TYPES.ERROR);
  }

  static info(vm, message) {
    vm.$root.$emit('show-island-message', message, MESSAGE_TYPES.INFO);
  }

  static showValidationErrors(vm, errors) {
    if (errors && errors.length > 0) {
      this.warning(vm, errors[0]);
    }
  }

  static loginSuccess(vm, username) {
    const message = username ? `欢迎回来，${username}！` : '登录成功！';
    this.success(vm, message);
    vm.$root.$emit('celebrate-login');
  }
}

export class ConfirmHelper {
  static confirm(vm, message, title = '提示', options = {}) {
    return vm.$confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'glass-message-box',
      ...options
    });
  }

  static confirmDelete(vm, itemName) {
    return this.confirm(vm, `确定要删除${itemName}吗？`, '删除确认');
  }

  static confirmResetPassword(vm, userName) {
    return this.confirm(vm, `确定要重置用户 ${userName} 的密码吗？`, '重置密码确认');
  }

  static confirmStatusToggle(vm, userName, isActive) {
    const action = isActive ? '封禁' : '解除封禁';
    return this.confirm(vm, `确定要${action}用户 ${userName} 吗？`, `${action}确认`, {
      type: isActive ? 'warning' : 'info'
    });
  }

  static confirmApprove(vm) {
    return this.confirm(vm, '确定要通过该商品的审核吗？通过后将生成交换凭证码。', '审核确认', {
      type: 'success', confirmButtonText: '通过'
    });
  }
}