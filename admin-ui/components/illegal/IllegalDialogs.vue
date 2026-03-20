<template>
  <div>
    <!-- 举报详情弹窗 -->
    <IllegalDetailDialog
      :visible="detailVisible"
      :report="currentDetail"
      @update:visible="$emit('update:detail-visible', $event)"
      @show-reject-dialog="$emit('show-reject-dialog', $event)"
      @set-processing="$emit('set-processing', $event)"
      @show-action-dialog="$emit('show-action-dialog', $event)"
      @dialog-open="$emit('dialog-open')"
      @dialog-close="$emit('dialog-close')"
    />

    <!-- 处理进度弹窗 -->
    <IllegalProgressDialog
      :visible="progressVisible"
      :report="currentProgress"
      :progress-text="progressText"
      @update:visible="$emit('update:progress-visible', $event)"
      @update:progress-text="$emit('update:progress-text', $event)"
      @save-progress="$emit('save-progress')"
      @close="$emit('close-progress')"
      @dialog-open="$emit('dialog-open')"
      @dialog-close="$emit('dialog-close')"
    />

    <!-- 驳回理由弹窗 -->
    <IllegalRejectDialog
      :visible="rejectVisible"
      :report="currentReject"
      :reject-reason="rejectReason"
      @update:visible="$emit('update:reject-visible', $event)"
      @update:reject-reason="$emit('update:reject-reason', $event)"
      @confirm-reject="$emit('confirm-reject')"
      @close="$emit('close-reject')"
      @dialog-open="$emit('dialog-open')"
      @dialog-close="$emit('dialog-close')"
    />

    <!-- 处理行动选择弹窗 -->
    <IllegalActionDialog
      :visible="actionVisible"
      :report="currentAction"
      :selected-action="selectedAction"
      :action-params="actionParams"
      :action-feedback="actionFeedback"
      :is-executing="isExecuting"
      @update:visible="$emit('update:action-visible', $event)"
      @update:selected-action="$emit('update:selected-action', $event)"
      @select-action="$emit('select-action', $event)"
      @select-duration="$emit('select-duration', $event)"
      @validate-credit="$emit('validate-credit', $event)"
      @cancel-action="$emit('cancel-action')"
      @execute-action="$emit('execute-action')"
      @dialog-open="$emit('dialog-open')"
      @dialog-close="$emit('dialog-close')"
    />

    <!-- 处理结果弹窗 -->
    <IllegalResultDialog
      :visible="resultVisible"
      :report="currentResult"
      @update:visible="$emit('update:result-visible', $event)"
      @close="$emit('close-result')"
      @dialog-open="$emit('dialog-open')"
      @dialog-close="$emit('dialog-close')"
    />
  </div>
</template>

<script>
import IllegalDetailDialog from './dialogs/IllegalDetailDialog.vue'
import IllegalProgressDialog from './dialogs/IllegalProgressDialog.vue'
import IllegalRejectDialog from './dialogs/IllegalRejectDialog.vue'
import IllegalActionDialog from './dialogs/IllegalActionDialog.vue'
import IllegalResultDialog from './dialogs/IllegalResultDialog.vue'

export default {
  name: 'IllegalDialogs',
  
  components: {
    IllegalDetailDialog,
    IllegalProgressDialog,
    IllegalRejectDialog,
    IllegalActionDialog,
    IllegalResultDialog
  },
  
  props: {
    detailVisible: Boolean,
    progressVisible: Boolean,
    rejectVisible: Boolean,
    actionVisible: Boolean,
    resultVisible: Boolean,
    currentDetail: Object,
    currentProgress: Object,
    currentReject: Object,
    currentAction: Object,
    currentResult: Object,
    progressText: String,
    rejectReason: String,
    selectedAction: String,
    actionParams: Object,
    actionFeedback: Object,
    isExecuting: Boolean
  }
}
</script>