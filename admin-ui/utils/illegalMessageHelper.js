import { MessageHelper } from './messageHelper'

export const OPERATION_MESSAGES = {
  REPORT_APPROVED: '举报已处理',
  REPORT_REJECTED: '举报已驳回',
  REPORT_SET_PROCESSING: '举报已转为处理中',
  PROGRESS_SAVED: '处理进度已保存',
  PROCESSING_COMPLETED: '举报处理已完成',
  ACTION_EXECUTED: '处理完成',
  REJECT_REASON_REQUIRED: '请输入驳回理由',
  PROGRESS_CONTENT_REQUIRED: '请输入处理进度内容',
  CREDIT_SCORE_TOO_LOW: '信誉分数不能少于1分',
  CREDIT_SCORE_TOO_HIGH: '信誉分数不能超过100分',
  OPERATION_FAILED: '处理失败，请重试',
  EXPORT_DEVELOPING: '数据导出功能开发中',
  ACTION_SELECTED: '已选择',
  BAN_DURATION_SET: '封禁时长'
}

export class OperationMessageHelper {
  static showReportOperationSuccess(vm, operation) {
    const message = OPERATION_MESSAGES[operation]
    if (message) MessageHelper.success(vm, message)
  }

  static showActionCompleted(vm, actionLabel) {
    MessageHelper.success(vm, `${OPERATION_MESSAGES.ACTION_EXECUTED}：${actionLabel}`)
  }

  static showActionSelected(vm, actionLabel) {
    MessageHelper.info(vm, `${OPERATION_MESSAGES.ACTION_SELECTED}：${actionLabel}`)
  }

  static showBanDurationSet(vm, duration) {
    MessageHelper.info(vm, `${OPERATION_MESSAGES.BAN_DURATION_SET}：${duration}天`)
  }

  static showCreditScoreValidation(vm, type) {
    const message = type === 'low'
      ? OPERATION_MESSAGES.CREDIT_SCORE_TOO_LOW
      : OPERATION_MESSAGES.CREDIT_SCORE_TOO_HIGH
    MessageHelper.warning(vm, message)
  }

  static showRequiredFieldWarning(vm, field) {
    const messageMap = {
      rejectReason: OPERATION_MESSAGES.REJECT_REASON_REQUIRED,
      progressContent: OPERATION_MESSAGES.PROGRESS_CONTENT_REQUIRED
    }
    const message = messageMap[field]
    if (message) MessageHelper.warning(vm, message)
  }

  static showOperationFailed(vm) {
    MessageHelper.error(vm, OPERATION_MESSAGES.OPERATION_FAILED)
  }

  static showDevelopingFeature(vm, feature = '数据导出功能') {
    MessageHelper.info(vm, `${feature}开发中`)
  }
}
