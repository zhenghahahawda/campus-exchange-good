<template>
  <el-dialog
    :visible.sync="localVisible"
    :title="dialogTitle"
    width="520px"
    :close-on-click-modal="false"
    :lock-scroll="false"
    :modal="false"
    top="5vh"
    custom-class="glass-dialog"
    @close="handleClose"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="small">
      <el-form-item label="举报类型" prop="reportType">
        <el-select v-model="form.reportType" placeholder="请选择举报类型" style="width:100%">
          <el-option-group v-for="group in reportTypeOptions" :key="group.label" :label="group.label">
            <el-option v-for="opt in group.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-form-item label="举报标题" prop="title">
        <el-input v-model="form.title" placeholder="简要描述问题" maxlength="50" show-word-limit />
      </el-form-item>

      <el-form-item label="问题描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3"
          placeholder="请描述具体问题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="详细说明">
        <el-input v-model="form.detailContent" type="textarea" :rows="2"
          placeholder="可选，补充更多细节" maxlength="500" />
      </el-form-item>

      <!-- 证据图片 -->
      <el-form-item label="证据图片">
        <div class="evidence-area">
          <div v-for="(url, i) in evidenceImages" :key="i" class="evidence-thumb">
            <img :src="url" />
            <span class="remove-btn" @click="removeEvidence(i)"><i class="el-icon-close"></i></span>
          </div>
          <div v-if="evidenceImages.length < 3" class="evidence-upload" @click="triggerUpload">
            <i v-if="!uploading" class="el-icon-plus"></i>
            <i v-else class="el-icon-loading"></i>
            <span>{{ uploading ? '上传中' : `${evidenceImages.length}/3` }}</span>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
        <div class="evidence-tip">最多上传3张截图作为证据</div>
      </el-form-item>
    </el-form>

    <span slot="footer">
      <button class="glass-btn btn-cancel" @click="handleClose"><i class="el-icon-close"></i> 取消</button>
      <button class="glass-btn btn-danger" :disabled="submitting" @click="handleSubmit"><i class="el-icon-warning"></i> 提交举报</button>
    </span>
  </el-dialog>
</template>

<script>
const PRODUCT_TYPES = [
  { value: 'product-fake', label: '虚假商品信息' },
  { value: 'product-prohibited', label: '违禁物品' },
  { value: 'product-copyright', label: '侵权商品' },
  { value: 'product-price', label: '价格欺诈' },
  { value: 'product-quality', label: '质量问题' },
  { value: 'product-description', label: '描述不符' }
]
const USER_TYPES = [
  { value: 'user-harassment', label: '骚扰行为' },
  { value: 'user-fraud', label: '欺诈行为' },
  { value: 'user-spam', label: '垃圾信息' },
  { value: 'user-impersonation', label: '冒充他人' },
  { value: 'user-inappropriate', label: '不当内容' },
  { value: 'user-transaction', label: '交易纠纷' }
]

export default {
  name: 'ReportDialog',
  props: {
    visible: { type: Boolean, default: false },
    targetType: { type: String, default: 'product' },
    targetId: { type: [Number, String], required: true }
  },
  data() {
    return {
      submitting: false,
      uploading: false,
      evidenceImages: [],
      form: { reportType: '', title: '', description: '', detailContent: '' },
      rules: {
        reportType: [{ required: true, message: '请选择举报类型', trigger: 'change' }],
        title: [{ required: true, message: '请填写举报标题', trigger: 'blur' }],
        description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
      }
    }
  },
  computed: {
    localVisible: {
      get() { return this.visible },
      set(v) { this.$emit('update:visible', v) }
    },
    dialogTitle() {
      return this.targetType === 'product' ? '举报商品' : '举报用户'
    },
    reportTypeOptions() {
      if (this.targetType === 'product') return [{ label: '商品问题', options: PRODUCT_TYPES }]
      return [{ label: '用户问题', options: USER_TYPES }]
    }
  },
  methods: {
    triggerUpload() {
      if (this.uploading) return
      this.$refs.fileInput.value = ''
      this.$refs.fileInput.click()
    },
    async handleFileChange(e) {
      const file = e.target.files[0]
      if (!file) return
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片不能超过5MB')
        return
      }
      this.uploading = true
      try {
        const fd = new FormData()
        fd.append('file', file)
        const res = await this.$axios.post('/violations/upload-evidence', fd, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.code === 20000 && res.data) {
          this.evidenceImages.push(res.data)
        }
      } catch (e) {
        this.$message.error('图片上传失败')
      } finally {
        this.uploading = false
      }
    },
    removeEvidence(i) {
      this.evidenceImages.splice(i, 1)
    },
    handleClose() {
      this.$refs.form && this.$refs.form.resetFields()
      this.form.detailContent = ''
      this.evidenceImages = []
      this.localVisible = false
    },
    async handleSubmit() {
      const valid = await this.$refs.form.validate().catch(() => false)
      if (!valid) return
      this.submitting = true
      try {
        const res = await this.$axios.post('/violations', {
          targetType: this.targetType,
          targetId: Number(this.targetId),
          reportType: this.form.reportType,
          title: this.form.title,
          description: this.form.description,
          detailContent: this.form.detailContent || undefined,
          evidenceImages: this.evidenceImages.length ? this.evidenceImages : undefined
        })
        this.$message.success(`举报已提交，编号：${res.data}`)
        this.handleClose()
        this.$emit('success')
      } catch (e) {
        this.$message.error('提交失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss">
.report-dialog {
  .el-dialog__header { padding-bottom: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); }
  .el-dialog__body { padding: 20px 24px 8px; }
}
</style>

<style lang="scss" scoped>
.evidence-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.evidence-thumb {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(255,255,255,0.15);

  img { width: 100%; height: 100%; object-fit: cover; }

  .remove-btn {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: rgba(0,0,0,0.6);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 11px;
    &:hover { background: #f56c6c; }
  }
}

.evidence-upload {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px dashed rgba(255,255,255,0.25);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  gap: 4px;
  color: rgba(255,255,255,0.4);
  font-size: 11px;
  transition: all 0.2s;

  i { font-size: 20px; }
  &:hover { border-color: var(--color-primary); color: var(--color-primary); }
}

.evidence-tip {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(255,255,255,0.3);
}
</style>
<style>
::v-deep .glass-dialog {
  background: var(--glass-bg) !important;
  backdrop-filter: blur(var(--glass-blur, 20px));
  -webkit-backdrop-filter: blur(var(--glass-blur, 20px));
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  border-radius: 20px !important;
}
::v-deep .glass-dialog .el-dialog__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--glass-border);
}
::v-deep .glass-dialog .el-dialog__title {
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}
::v-deep .glass-dialog .el-dialog__headerbtn .el-dialog__close {
  color: var(--color-text-secondary);
  &:hover { color: var(--color-primary); }
}
::v-deep .glass-dialog .el-dialog__body {
  padding: 24px;
  background: transparent;
  color: var(--color-text-primary);
}
::v-deep .glass-dialog .el-dialog__footer {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
::v-deep .glass-dialog .el-input__inner,
::v-deep .glass-dialog .el-textarea__inner {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--glass-border);
  color: var(--color-text-primary);
  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
  }
}
::v-deep .glass-dialog .el-input .el-input__count {
  background: transparent;
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
}
::v-deep .glass-dialog .el-textarea .el-input__count {
  background: transparent;
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
  bottom: 4px;
  right: 0;
}
::v-deep .glass-dialog .el-form-item__label {
  color: var(--color-text-secondary);
}
::v-deep .glass-dialog .el-select .el-input__inner {
  cursor: pointer;
}

.glass-btn {
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &.btn-cancel {
    background: rgba(var(--color-text-secondary, 203, 213, 225), 0.1);
    border: 1px solid var(--glass-border);
    color: var(--color-text-secondary);
    &:hover { background: rgba(var(--color-text-secondary, 203, 213, 225), 0.2); }
  }

  &.btn-danger {
    background: linear-gradient(135deg, rgba(var(--color-secondary-danger-rgb), 0.95), rgba(var(--color-secondary-danger-rgb), 0.8));
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-secondary-danger-rgb), 0.3);
    &:hover { box-shadow: 0 6px 20px rgba(var(--color-secondary-danger-rgb), 0.45); transform: translateY(-1px); }
    &:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
  }
}
</style>