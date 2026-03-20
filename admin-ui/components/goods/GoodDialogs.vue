<template>
  <div class="dialogs-wrapper">
    <!-- 驳回理由弹窗 -->
    <el-dialog
      title="驳回商品审核"
      :visible="rejectVisible"
      @update:visible="$emit('update:rejectVisible', $event)"
      width="500px"
      custom-class="glass-dialog reject-dialog"
      append-to-body
      :modal="false"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <p class="dialog-hint">请填写驳回理由，将通知卖家修改：</p>
        <el-input
          type="textarea"
          :rows="6"
          placeholder="例如：图片不清晰、描述不符、包含违禁信息等"
          :value="rejectReason"
          @input="$emit('update:rejectReason', $event)"
          class="reject-textarea"
        >
        </el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:rejectVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-reject" @click="$emit('confirm-reject')">
          <i class="el-icon-warning"></i>
          <span>确认驳回</span>
        </button>
      </span>
    </el-dialog>

    <!-- 编辑/新增商品弹窗 -->
    <el-dialog
      :title="currentForm.id ? '编辑商品' : '新增商品'"
      :visible="formVisible"
      @update:visible="$emit('update:formVisible', $event)"
      width="750px"
      custom-class="glass-dialog product-dialog"
      top="5vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <!-- 编辑/新增表单 -->
      <el-form :model="currentForm" label-width="100px" label-position="left" class="glass-form product-form">
        <!-- 商品名称 -->
        <el-form-item label="商品名称" required class="form-item-highlight">
          <el-input 
            v-model="currentForm.name" 
            placeholder="请输入商品名称"
            maxlength="50"
            show-word-limit
          >
            <i slot="prefix" class="el-icon-goods"></i>
          </el-input>
        </el-form-item>
        
        <!-- 新旧程度和分类 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="新旧程度" required class="condition-item">
              <div class="condition-button-group">
                <div 
                  v-for="option in conditionOptions" 
                  :key="option.value"
                  :class="['condition-button', { 'is-active': currentForm.condition === option.value }]"
                  @click="currentForm.condition = option.value"
                >
                  <span class="button-label">{{ option.label }}</span>
                  <span class="button-desc">{{ option.description }}</span>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 分类 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="商品分类" required class="category-item">
              <CategorySelector
                v-model="currentForm.categoryId"
                :show-all-option="false"
                placeholder="请选择分类"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 地区、学校和交付方式 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="所在地区" required class="region-item">
              <el-input 
                v-model="currentForm.region" 
                placeholder="例如：北京市"
              >
                <i slot="prefix" class="el-icon-location-outline"></i>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所在学校" required class="school-item">
              <el-input 
                v-model="currentForm.school" 
                placeholder="例如：清华大学"
              >
                <i slot="prefix" class="el-icon-school"></i>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交付方式" required class="delivery-item">
              <el-select 
                v-model="currentForm.deliveryType" 
                placeholder="请选择"
                style="width: 100%"
              >
                <el-option label="自提" value="pickup"></el-option>
                <el-option label="快递" value="express"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 交换凭证码 (仅编辑模式且已审核通过的商品显示) -->
        <el-row v-if="currentForm.exchangeCode" :gutter="20">
          <el-col :span="24">
            <el-form-item label="交换凭证" class="exchange-code-item">
              <div class="exchange-code-input-wrapper">
                <el-input 
                  v-model="currentForm.exchangeCode" 
                  placeholder="交换凭证码"
                  readonly
                  class="code-input"
                >
                  <i slot="prefix" class="el-icon-tickets"></i>
                </el-input>
              </div>
              <div class="code-hint">
                <i class="el-icon-info"></i>
                <span>此凭证码用于线下交易验证</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 审核与时间信息（仅编辑模式显示） -->
        <el-row v-if="currentForm.id" :gutter="20" class="audit-time-section">
          <el-col :span="24">
            <div class="section-label">
              <i class="el-icon-time"></i>
              <span>时间与审核信息</span>
            </div>
            <div class="time-info-grid">
              <div class="time-info-item" v-if="currentForm.createTime">
                <span class="time-label">创建时间</span>
                <span class="time-value">{{ currentForm.createTime }}</span>
              </div>
              <div class="time-info-item" v-if="currentForm.updateTime">
                <span class="time-label">更新时间</span>
                <span class="time-value">{{ currentForm.updateTime }}</span>
              </div>
              <div class="time-info-item" v-if="currentForm.auditTime">
                <span class="time-label">审核时间</span>
                <span class="time-value">{{ currentForm.auditTime }}</span>
              </div>
              <div class="time-info-item" v-if="currentForm.auditTime || currentForm.auditorId">
                <span class="time-label">审核人ID</span>
                <span class="time-value">{{ currentForm.auditorId ? '#' + currentForm.auditorId : 'admin' }}</span>
              </div>
              <div class="time-info-item" v-if="currentForm.soldTime">
                <span class="time-label">售出时间</span>
                <span class="time-value sold">{{ currentForm.soldTime }}</span>
              </div>
              <div class="time-info-item full-width" v-if="currentForm.rejectReason">
                <span class="time-label">驳回原因</span>
                <span class="time-value reject">{{ currentForm.rejectReason }}</span>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 商品描述 -->
        <el-form-item label="商品描述" required class="description-item">
          <el-input 
            type="textarea" 
            :rows="4" 
            v-model="currentForm.description"
            placeholder="请输入商品描述..."
            maxlength="500"
            show-word-limit
            class="description-textarea"
          ></el-input>
        </el-form-item>
        
        <!-- 商品图片 -->
        <el-form-item label="商品图片" required class="image-item">
          <div class="image-upload-container">
            <div 
              v-for="(img, index) in imageList" 
              :key="index" 
              class="image-item-box"
            >
              <img :src="img" class="preview-img" alt="商品图片">
              <div class="image-overlay">
                <i class="el-icon-zoom-in" @click="previewImage(img)"></i>
                <i class="el-icon-delete" @click="removeImage(index)"></i>
              </div>
              <div class="image-index">{{ index + 1 }}</div>
            </div>
            
            <div 
              v-if="imageList.length < 3" 
              class="upload-box"
              @click="handleUploadClick"
            >
              <i class="el-icon-plus upload-icon"></i>
              <span class="upload-text">上传图片</span>
              <span class="upload-count">{{ imageList.length }}/3</span>
            </div>
          </div>
          <!-- 隐藏的文件输入框 -->
          <input 
            type="file" 
            ref="fileInput" 
            style="display: none" 
            accept="image/jpeg,image/png,image/jpg" 
            multiple
            @change="handleFileChange"
          >
          <div class="upload-tips">
            <i class="el-icon-info"></i>
            <span>最多上传3张图片，支持 JPG、PNG 格式，单张不超过 5MB</span>
          </div>
        </el-form-item>
      </el-form>
      
      <span slot="footer" class="dialog-footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:formVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-save" @click="$emit('save-product')">
          <i class="el-icon-check"></i>
          <span>保存提交</span>
        </button>
      </span>
    </el-dialog>
    
    <!-- 图片预览弹窗 -->
    <el-dialog
      :visible.sync="previewVisible"
      width="40%"
      custom-class="preview-dialog"
      :modal="false"
      append-to-body
    >
      <img :src="previewImageUrl" style="width: 100%; display: block; max-height: 60vh; object-fit: contain;">
    </el-dialog>
  </div>
</template>

<script>
import { CONDITION_OPTIONS } from '@/utils/constants'
import CategorySelector from '@/components/business/CategorySelector.vue'
import { isSuccess } from '@/composables/useApi'

export default {
  name: 'GoodDialogs',
  components: {
    CategorySelector
  },
  props: {
    rejectVisible: {
      type: Boolean,
      default: false
    },
    rejectReason: {
      type: String,
      default: ''
    },
    formVisible: {
      type: Boolean,
      default: false
    },
    isEditMode: {
      type: Boolean,
      default: false
    },
    currentForm: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      previewVisible: false,
      previewImageUrl: '',
      conditionOptions: CONDITION_OPTIONS
    }
  },
  computed: {
    imageList() {
      // 优先使用 images 数组
      if (this.currentForm.images && Array.isArray(this.currentForm.images)) {
        return this.currentForm.images;
      }
      // 兼容旧字段
      if (!this.currentForm.image) return [];
      if (Array.isArray(this.currentForm.image)) {
        return this.currentForm.image;
      }
      return [this.currentForm.image];
    }
  },
  methods: {
    handleUploadClick() {
      // 触发文件选择
      this.$refs.fileInput.click();
    },
    async handleFileChange(e) {
      const files = e.target.files;
      if (!files.length) return;

      const currentCount = this.imageList.length;
      const remainingSlots = 3 - currentCount;

      // 如果选择的文件数超过剩余槽位，只取前面的几个
      if (files.length > remainingSlots) {
        this.$root.$emit('show-island-message', `最多只能再上传 ${remainingSlots} 张图片，已自动截取`, 'warning');
      }

      // 转换为数组并截取允许的数量
      const filesToProcess = Array.from(files).slice(0, remainingSlots);

      for (const file of filesToProcess) {
        // 检查大小 (5MB)
        if (file.size > 5 * 1024 * 1024) {
           this.$root.$emit('show-island-message', `图片 ${file.name} 超过 5MB`, 'warning');
           continue;
        }

        try {
          // 准备上传数据
          const formData = new FormData();
          formData.append('file', file);

          // 调用上传接口
          const res = await this.$axios.post('/api/goods/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });

          // 根据接口返回格式获取 URL (假设返回结构是 { code: 200, data: "url" })
          if (isSuccess(res)) {
            if (!this.currentForm.images) {
              this.$set(this.currentForm, 'images', []);
            }
            // 确保是 URL 字符串
            const url = res.data;
            this.currentForm.images.push(url);
            this.$root.$emit('show-island-message', '图片上传成功', 'success');
          } else {
            this.$root.$emit('show-island-message', res.message || '图片上传失败', 'danger');
          }
        } catch (error) {
          console.error('上传图片出错:', error);
          this.$root.$emit('show-island-message', '图片上传服务异常', 'danger');
        }
      }

      // 清空 input，允许重复选择同一文件
      e.target.value = '';
    },
    removeImage(index) {
      if (this.currentForm.images && Array.isArray(this.currentForm.images)) {
          this.currentForm.images.splice(index, 1);
      } else {
          // 兼容旧字段逻辑
          const images = [...this.imageList];
          images.splice(index, 1);
          this.currentForm.image = images.length > 0 ? images : '';
      }
    },
    previewImage(url) {
      this.previewImageUrl = url;
      this.previewVisible = true;
    },
    regenerateCode() {
      // 生成新的凭证码
      const schoolCode = this.currentForm.school ? this.currentForm.school.substring(0, 2) : 'XX';
      const date = new Date();
      const dateStr = `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`;
      const randomCode = Math.random().toString(36).substring(2, 6).toUpperCase();
      this.currentForm.exchangeCode = `EX-${schoolCode}-${dateStr}-${randomCode}`;
      
      this.$root.$emit('show-island-message', '凭证码已重新生成', 'success');
    },
    getStatusLabel(status) {
      const labels = {
        'pending': '待审核',
        'active': '出售中',
        'sold': '已售出',
        'off_shelf': '已下架',
        'rejected': '已驳回'
      };
      return labels[status] || status;
    },
    getStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'active': 'success',
        'sold': 'info',
        'off_shelf': 'info',
        'rejected': 'danger'
      };
      return typeMap[status] || 'info';
    },
    getConditionLabel(condition) {
      const option = this.conditionOptions.find(opt => opt.value === condition);
      return option ? option.label : condition;
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

// 定义金橙色主题变量
$color-gold: #F59E0B;
$color-gold-rgb: 245, 158, 11;
$color-text-dark: #333;
$color-text-light: #666;

// 紧凑型商品详情样式
.product-detail-compact {
  .detail-header {
    padding: 16px 20px;
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.08), rgba(var(--color-primary-rgb), 0.03));
    border-radius: 12px;
    margin-bottom: 20px;
    border: 1px solid rgba(var(--color-primary-rgb), 0.15);
    
    .header-left {
      .product-name {
        font-size: 18px;
        font-weight: 700;
        color: $color-text-primary;
        margin: 0 0 10px 0;
      }
      
      .product-tags {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
        
        .tag-item {
          font-size: 12px;
          padding: 4px 10px;
          border-radius: 6px;
          background: rgba($color-primary, 0.1);
          color: $color-primary;
          font-weight: 500;
        }
      }
    }
  }
  
  .detail-section {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid rgba($color-border, 0.2);
      
      i {
        font-size: 15px;
        color: $color-primary;
      }
    }
    
    .images-compact {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      
      .image-thumb {
        position: relative;
        width: 90px;
        height: 90px;
        border-radius: 10px;
        overflow: hidden;
        border: 2px solid rgba($color-border, 0.3);
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: $color-primary;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.2);
          
          .image-mask {
            opacity: 1;
          }
        }
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .image-mask {
          position: absolute;
          inset: 0;
          background: rgba(0, 0, 0, 0.6);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s ease;
          
          i {
            font-size: 24px;
            color: #fff;
          }
        }
      }
    }
    
    .info-grid-compact {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 14px;
        background: rgba(var(--color-bg-surface), 0.5);
        border-radius: 8px;
        border: 1px solid rgba($color-border, 0.15);
        
        .label {
          font-size: 12px;
          color: $color-text-secondary;
        }
        
        .value {
          font-size: 13px;
          color: $color-text-primary;
          font-weight: 600;
        }
      }
    }
    
    .exchange-code-compact {
      padding: 12px 16px;
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.08), rgba(var(--color-primary-rgb), 0.05));
      border: 2px solid rgba(var(--color-primary-rgb), 0.3);
      border-radius: 10px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      
      .code-text {
        font-size: 16px;
        font-weight: 700;
        color: $color-primary;
        font-family: 'Courier New', monospace;
        letter-spacing: 1px;
      }
      
      .code-hint {
        font-size: 11px;
        color: $color-text-secondary;
      }
    }
    
    .description-text {
      padding: 12px 14px;
      background: rgba(var(--color-bg-page), 0.5);
      border-radius: 8px;
      font-size: 13px;
      color: $color-text-secondary;
      line-height: 1.6;
      max-height: 120px;
      overflow-y: auto;
      
      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(var(--color-primary-rgb), 0.3);
        border-radius: 2px;
      }
    }
  }
}

// 产品表单特殊样式
.product-form {
  padding: 10px 20px;

  // 通用表单项样式
  ::v-deep .el-form-item {
    margin-bottom: 24px;
    
    .el-form-item__label {
      color: $color-text-secondary;
      font-weight: 600;
      font-size: 13px;
      line-height: 40px;
      opacity: 0.85;
    }
    
    .el-form-item__content {
      line-height: 40px;
    }
  }

  // 高亮表单项（商品名称）
  .form-item-highlight {
    ::v-deep .el-input__inner {
      font-size: 16px;
      font-weight: 600;
      color: $color-text-primary;
      padding-left: 40px;
      padding-right: 70px; // 给字数统计留出空间
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba($color-border, 0.3);
      border-radius: 12px;
      height: 48px;
      transition: all 0.3s ease;
      
      &:focus {
        border-color: $color-primary;
        background: rgba(255, 255, 255, 0.08);
        box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
      }
    }
    
    ::v-deep .el-input__prefix {
      left: 12px;
      display: flex;
      align-items: center;
      
      i {
        font-size: 18px;
        color: $color-primary;
      }
    }
    
    ::v-deep .el-input__suffix {
      right: 12px;
      
      .el-input__count {
        background: transparent !important;
        color: $color-text-disabled;
        font-size: 12px;
        line-height: 48px;
        
        .el-input__count-inner {
          background: transparent !important;
          color: $color-text-disabled;
          line-height: 1;
        }
      }
    }
  }
  
  // 新旧程度按钮组
  .condition-item {
    ::v-deep .el-form-item__label {
      margin-bottom: 8px;
    }
    
    .condition-button-group {
      width: 100%;
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      
      .condition-button {
        display: flex;
        flex-direction: column;
        gap: 4px;
        padding: 14px 16px;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.05);
        border: 2px solid rgba($color-border, 0.3);
        cursor: pointer;
        transition: all 0.3s ease;
        user-select: none;
        
        .button-label {
          font-size: 15px;
          font-weight: 600;
          color: $color-text-primary;
        }
        
        .button-desc {
          font-size: 12px;
          color: $color-text-secondary;
          line-height: 1.4;
        }
        
        &:hover {
          background: rgba(255, 255, 255, 0.08);
          border-color: rgba($color-primary, 0.5);
        }
        
        &.is-active {
          background: rgba(var(--color-primary-rgb), 0.1);
          border-color: $color-primary;
          box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
          
          .button-label {
            color: $color-primary;
          }
        }
      }
    }
  }

  // 分类
  .category-item {
    ::v-deep .el-select .el-input__inner {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba($color-border, 0.3);
      border-radius: 12px;
      color: $color-text-primary;
      height: 48px;
      font-size: 14px;
      transition: all 0.3s ease;

      &:focus {
        background: rgba(255, 255, 255, 0.08);
        border-color: $color-primary;
        box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
      }
    }

    ::v-deep .el-select .el-input__suffix i {
      color: $color-text-secondary;
    }
  }

  // 审核与时间信息区域
  .audit-time-section {
    margin-bottom: 24px;

    .section-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid rgba($color-border, 0.2);

      i {
        font-size: 16px;
        color: $color-primary;
      }
    }

    .time-info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .time-info-item {
        display: flex;
        flex-direction: column;
        gap: 6px;
        padding: 12px 16px;
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid rgba($color-border, 0.2);
        border-radius: 10px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.05);
          border-color: rgba($color-border, 0.3);
        }

        &.full-width {
          grid-column: 1 / -1;
        }

        .time-label {
          font-size: 12px;
          color: $color-text-secondary;
          font-weight: 500;
        }

        .time-value {
          font-size: 13px;
          color: $color-text-primary;
          font-weight: 600;
          font-family: 'Courier New', monospace;

          &.sold {
            color: $color-secondary-success;
          }

          &.reject {
            color: $color-secondary-danger;
            font-family: inherit;
            word-break: break-word;
            line-height: 1.5;
          }
        }
      }
    }
  }

  // 地区和学校
  .region-item,
  .school-item,
  .delivery-item {
    ::v-deep .el-input__inner,
    ::v-deep .el-select .el-input__inner {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba($color-border, 0.3);
      border-radius: 12px;
      color: $color-text-primary;
      height: 48px;
      font-size: 14px;
      padding-left: 40px;
      transition: all 0.3s ease;
      
      &:focus {
        background: rgba(255, 255, 255, 0.08);
        border-color: $color-primary;
        box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
      }
      
      &::placeholder {
        color: $color-text-disabled;
      }
    }
    
    ::v-deep .el-input__prefix {
      left: 12px;
      display: flex;
      align-items: center;
      
      i {
        font-size: 16px;
        color: $color-primary;
      }
    }
  }

  // 交付方式
  .delivery-item {
     ::v-deep .el-input__inner {
       padding-left: 15px; // 移除图标占位，因为它没有图标
     }
  }
  
  // 交换凭证码
  .exchange-code-item {
    .exchange-code-input-wrapper {
      display: flex;
      gap: 12px;
      align-items: center;
      
      .code-input {
        flex: 1;
        
        ::v-deep .el-input__inner {
          background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.08), rgba(var(--color-primary-rgb), 0.05));
          border: 2px solid rgba(var(--color-primary-rgb), 0.3);
          border-radius: 12px;
          color: $color-primary;
          height: 48px;
          font-size: 14px;
          font-weight: 600;
          font-family: 'Courier New', monospace;
          letter-spacing: 1px;
          padding-left: 40px;
          cursor: default;
          
          &:focus {
            border-color: $color-primary;
            box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
          }
        }
        
        ::v-deep .el-input__prefix {
          left: 12px;
          display: flex;
          align-items: center;
          
          i {
            font-size: 18px;
            color: $color-primary;
          }
        }
      }
      
      .regenerate-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 12px 20px;
        background: rgba(var(--color-primary-rgb), 0.1);
        border: 1px solid rgba(var(--color-primary-rgb), 0.3);
        border-radius: 12px;
        color: $color-primary;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        white-space: nowrap;
        outline: none;
        
        i {
          font-size: 16px;
          transition: transform 0.3s ease;
        }
        
        &:hover {
          background: rgba(var(--color-primary-rgb), 0.15);
          border-color: $color-primary;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.2);
          
          i {
            transform: rotate(180deg);
          }
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
    
    .code-hint {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 8px;
      padding: 8px 12px;
      background: rgba($color-secondary-info, 0.05);
      border-radius: 8px;
      font-size: 12px;
      color: $color-text-secondary;
      
      i {
        font-size: 14px;
        color: $color-secondary-info;
      }
    }
  }
  
  // 描述文本域
  .description-item {
    ::v-deep .el-textarea__inner {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba($color-border, 0.3);
      border-radius: 12px;
      padding: 14px;
      font-size: 14px;
      color: $color-text-primary;
      line-height: 1.6;
      resize: none;
      transition: all 0.3s ease;
      
      &:focus {
        background: rgba(255, 255, 255, 0.08);
        border-color: $color-primary;
        box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
      }
      
      &::placeholder {
        color: $color-text-disabled;
      }
      
      // 自定义滚动条样式
      &::-webkit-scrollbar {
        width: 8px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba($color-border, 0.1);
        border-radius: 10px;
        margin: 4px 0;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(var(--color-primary-rgb), 0.3);
        border-radius: 10px;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(var(--color-primary-rgb), 0.5);
        }
        
        &:active {
          background: rgba(var(--color-primary-rgb), 0.7);
        }
      }
    }
    
    ::v-deep .el-input__count {
      background: transparent;
      color: $color-text-disabled;
      font-size: 12px;
    }
  }
}

// 图片上传容器
.image-upload-container {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  
  .image-item-box {
    position: relative;
    width: 110px;
    height: 110px;
    border-radius: 14px;
    overflow: hidden;
    border: 2px solid rgba($color-border, 0.3);
    background: rgba($color-bg-surface, 0.5);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      border-color: $color-primary;
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.2);
      
      .image-overlay {
        opacity: 1;
      }
    }
    
    .preview-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .image-index {
      position: absolute;
      top: 8px;
      left: 8px;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(8px);
      color: #fff;
      font-size: 13px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
    
    .image-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(6px);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      opacity: 0;
      transition: opacity 0.3s ease;
      
      i {
        font-size: 22px;
        color: #fff;
        cursor: pointer;
        padding: 10px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.15);
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 255, 255, 0.25);
          transform: scale(1.15);
        }
        
        &.el-icon-zoom-in:hover {
          color: $color-primary;
        }
        
        &.el-icon-delete:hover {
          color: $color-secondary-danger;
        }
      }
    }
  }
  
  .upload-box {
    width: 110px;
    height: 110px;
    border-radius: 14px;
    border: 2px dashed rgba($color-border, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: rgba($color-primary, 0.03);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      border-color: $color-primary;
      background: rgba($color-primary, 0.08);
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.15);
      
      .upload-icon {
        color: $color-primary;
        transform: scale(1.15);
      }
    }
    
    .upload-icon {
      font-size: 32px;
      color: $color-text-secondary;
      margin-bottom: 6px;
      transition: all 0.3s ease;
    }
    
    .upload-text {
      font-size: 13px;
      font-weight: 500;
      color: $color-text-secondary;
      margin-bottom: 4px;
    }
    
    .upload-count {
      font-size: 11px;
      color: $color-text-disabled;
      padding: 2px 8px;
      background: rgba($color-border, 0.2);
      border-radius: 10px;
      margin-top: 2px;
    }
  }
}

.upload-tips {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: $color-text-secondary;
  padding: 10px 14px;
  background: rgba($color-secondary-info, 0.05);
  border-radius: 10px;
  border: 1px solid rgba($color-secondary-info, 0.15);
  
  i {
    font-size: 15px;
    color: $color-secondary-info;
  }
}

// 对话框底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding-top: 10px;
  
  .glass-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 32px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
    backdrop-filter: blur(10px);
    
    i {
      font-size: 16px;
    }
    
    &.btn-cancel {
      background: rgba($color-text-secondary, 0.1);
      border: 1px solid rgba($color-border, 0.3);
      color: $color-text-secondary;
      
      &:hover {
        background: rgba($color-text-secondary, 0.15);
        border-color: rgba($color-border, 0.5);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
    
    &.btn-save {
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.85));
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      color: #fff;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      
      &:hover {
        background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 1), rgba(var(--color-primary-rgb), 0.9));
        border-color: rgba(var(--color-primary-rgb), 0.5);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    &.btn-reject {
      background: linear-gradient(135deg, rgba(var(--color-secondary-danger-rgb), 0.95), rgba(var(--color-secondary-danger-rgb), 0.85));
      border: 1px solid rgba(var(--color-secondary-danger-rgb), 0.3);
      color: #fff;
      box-shadow: 0 4px 12px rgba(var(--color-secondary-danger-rgb), 0.3);
      
      &:hover {
        background: linear-gradient(135deg, rgba(var(--color-secondary-danger-rgb), 1), rgba(var(--color-secondary-danger-rgb), 0.9));
        border-color: rgba(var(--color-secondary-danger-rgb), 0.5);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-secondary-danger-rgb), 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 驳回弹窗专属样式
::v-deep {
  .glass-dialog.reject-dialog {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    
    .el-dialog__header {
      padding: 24px 28px 20px;
      border-bottom: 1px solid rgba($color-border, 0.2);
      background: transparent; // 移除顶部背景色，保持一致
      
      .el-dialog__title {
        font-size: 18px;
        font-weight: 700;
        color: $color-text-primary;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '⚠️';
          font-size: 20px;
        }
      }
      
      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        width: 36px;
        height: 36px;
        
        .el-dialog__close {
          font-size: 20px;
          font-weight: 700;
          color: $color-text-secondary;
          
          &:hover {
            color: $color-secondary-danger;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 24px 28px;
      
      .dialog-content {
        .dialog-hint {
          font-size: 14px;
          color: $color-text-secondary;
          margin: 0 0 16px 0;
          line-height: 1.6;
          padding: 12px 16px;
          background: rgba(var(--color-secondary-warning-rgb), 0.08);
          border-radius: 8px;
        }
        
        .reject-textarea {
          .el-textarea__inner {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba($color-border, 0.3);
            border-radius: 12px;
            padding: 14px;
            font-size: 14px;
            color: $color-text-primary;
            line-height: 1.6;
            resize: none;
            transition: all 0.3s ease;
            
            &:focus {
              background: rgba(255, 255, 255, 0.08);
              border-color: $color-secondary-danger;
              box-shadow: 0 0 0 3px rgba(var(--color-secondary-danger-rgb), 0.1);
            }
            
            &::placeholder {
              color: $color-text-disabled;
            }
            
            // 自定义滚动条样式
            &::-webkit-scrollbar {
              width: 8px;
            }
            
            &::-webkit-scrollbar-track {
              background: rgba($color-border, 0.1);
              border-radius: 10px;
              margin: 4px 0;
            }
            
            &::-webkit-scrollbar-thumb {
              background: rgba(var(--color-secondary-danger-rgb), 0.3);
              border-radius: 10px;
              transition: all 0.3s ease;
              
              &:hover {
                background: rgba(var(--color-secondary-danger-rgb), 0.5);
              }
              
              &:active {
                background: rgba(var(--color-secondary-danger-rgb), 0.7);
              }
            }
          }
        }
      }
    }
    
    .el-dialog__footer {
      padding: 16px 28px 24px;
      background: transparent; // 移除底部背景色，保持一致
      border-top: 1px solid rgba($color-border, 0.2);
    }
  }
}

// 玻璃拟态/弹窗样式覆盖
::v-deep {
  .glass-dialog.product-dialog {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    
    .el-dialog__header {
      padding: 24px 28px 20px;
      border-bottom: 1px solid rgba($color-border, 0.2);
      background: rgba($color-primary, 0.02);
      
      .el-dialog__title {
        font-size: 20px;
        font-weight: 700;
        color: $color-text-primary;
      }
      
      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        width: 36px;
        height: 36px;
        
        .el-dialog__close {
          font-size: 20px;
          font-weight: 700;
          color: $color-text-secondary;
          
          &:hover {
            color: $color-primary;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 20px 28px;
      max-height: 70vh;
      overflow-y: auto;
      
      // 自定义滚动条样式 - 弹窗主体
      &::-webkit-scrollbar {
        width: 10px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba($color-border, 0.08);
        border-radius: 10px;
        margin: 8px 0;
        border: 2px solid transparent;
        background-clip: content-box;
      }
      
      &::-webkit-scrollbar-thumb {
        background: linear-gradient(180deg, 
          rgba(var(--color-primary-rgb), 0.4), 
          rgba(var(--color-primary-rgb), 0.3));
        border-radius: 10px;
        border: 2px solid transparent;
        background-clip: content-box;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: inset 0 0 6px rgba(var(--color-primary-rgb), 0.2);
        
        &:hover {
          background: linear-gradient(180deg, 
            rgba(var(--color-primary-rgb), 0.6), 
            rgba(var(--color-primary-rgb), 0.5));
          box-shadow: inset 0 0 8px rgba(var(--color-primary-rgb), 0.3),
                      0 0 12px rgba(var(--color-primary-rgb), 0.2);
        }
        
        &:active {
          background: linear-gradient(180deg, 
            rgba(var(--color-primary-rgb), 0.8), 
            rgba(var(--color-primary-rgb), 0.7));
          box-shadow: inset 0 0 10px rgba(var(--color-primary-rgb), 0.4);
        }
      }
    }
    
    .el-dialog__footer {
      padding: 16px 28px 24px;
      background: rgba($color-primary, 0.02);
      border-top: 1px solid rgba($color-border, 0.2);
    }
    
    // Input Number 样式
    .el-input-number {
      .el-input__inner {
        text-align: left;
      }
    }
  }
  
  .preview-dialog {
    @include glass-card;
    
    .el-dialog__body {
      padding: 0;
    }
    
    .el-dialog__headerbtn {
      background: rgba(0, 0, 0, 0.5);
      backdrop-filter: blur(10px);
      border-radius: 50%;
      width: 40px;
      height: 40px;
      top: 20px;
      right: 20px;
      
      .el-dialog__close {
        color: #fff;
        font-weight: 700;
        
        &:hover {
          color: $color-primary;
        }
      }
    }
  }
  
  .glass-button {
    @include glass-button;
  }
  
  .glass-button-danger {
    background: $color-secondary-danger;
    border-color: $color-secondary-danger;
    color: #fff;
    &:hover { opacity: 0.9; }
  }
  
  // 下拉框样式
  .glass-select-dropdown {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    
    .el-select-dropdown__item {
      color: $color-text-primary;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      gap: 10px;
      transition: all 0.3s ease;
      
      i {
        font-size: 16px;
        color: $color-primary;
      }
      
      &:hover {
        background: rgba($color-primary, 0.1);
      }
      
      &.selected {
        color: $color-primary;
        font-weight: 600;
        background: rgba($color-primary, 0.15);
      }
    }
  }
}
</style>
