<template>
  <el-dialog
    :visible.sync="visible"
    title="新增商品"
    width="800px"
    custom-class="publish-dialog"
    :close-on-click-modal="false"
    append-to-body
    @close="handleClose"
  >
    <div class="publish-form">
      <!-- 商品名称 -->
      <div class="form-item">
        <label class="required">商品名称</label>
        <div class="input-wrapper">
          <i class="el-icon-shopping-bag-1 prefix-icon"></i>
          <input 
            v-model="form.title" 
            type="text" 
            placeholder="请输入商品名称" 
            maxlength="50"
          />
          <span class="counter">{{ form.title.length }}/50</span>
        </div>
      </div>

      <!-- 新旧程度 -->
      <div class="form-item">
        <label class="required">新旧程度</label>
        <div class="condition-chips">
          <div 
            v-for="opt in conditionOptions" 
            :key="opt.value"
            class="chip-card"
            :class="{ active: form.condition === opt.value }"
            @click="form.condition = opt.value"
          >
            <div class="chip-title">{{ opt.label }}</div>
            <div class="chip-desc">{{ opt.desc }}</div>
          </div>
        </div>
      </div>

      <!-- 商品分类 -->
      <div class="form-item">
        <label class="required">商品分类</label>
        <el-select v-model="form.category" placeholder="请选择商品分类" class="full-width">
          <el-option 
            v-for="cat in categories" 
            :key="cat.value" 
            :label="cat.label" 
            :value="cat.value"
          />
        </el-select>
      </div>

      <!-- 所在地区 -->
      <div class="form-item">
        <label class="required">所在地区</label>
        <div class="input-wrapper">
          <i class="el-icon-location-outline prefix-icon gold"></i>
          <input 
            v-model="form.location" 
            type="text" 
            placeholder="例如：北京市" 
          />
        </div>
      </div>

      <!-- 学校与交付方式 -->
      <div class="form-row">
        <div class="form-item flex-1">
          <label class="required">所在学校</label>
          <div class="input-wrapper">
            <i class="el-icon-school prefix-icon gold"></i>
            <input 
              v-model="form.school" 
              type="text" 
              placeholder="例如：清华大学" 
            />
          </div>
        </div>
        <div class="form-item flex-1">
          <label class="required">交付方式</label>
          <el-select v-model="form.exchangeType" placeholder="请选择" class="full-width">
            <el-option label="自提" value="pickup"></el-option>
            <el-option label="邮寄" value="mail"></el-option>
            <el-option label="线上交付" value="online"></el-option>
          </el-select>
        </div>
      </div>

      <!-- 期望交换 -->
      <div class="form-item">
        <label>期望交换</label>
        <div class="input-wrapper">
          <i class="el-icon-refresh prefix-icon"></i>
          <input 
            v-model="form.exchangeFor" 
            type="text" 
            placeholder="描述你希望换到的物品，例如：考研资料、自行车..." 
            maxlength="100"
          />
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="form-item">
        <label class="required">商品描述</label>
        <div class="textarea-wrapper">
          <textarea 
            v-model="form.description" 
            placeholder="请输入商品描述..." 
            maxlength="500"
            rows="5"
          ></textarea>
          <span class="counter">{{ form.description.length }}/500</span>
        </div>
      </div>

      <!-- 商品图片 -->
      <div class="form-item">
        <label class="required">商品图片</label>
        <div class="image-uploader">
          <div class="image-list">
            <div 
              v-for="(img, index) in form.images" 
              :key="index" 
              class="image-preview"
            >
              <img :src="img.url" alt="preview" />
              <div class="delete-mask" @click="removeImage(index)">
                <i class="el-icon-delete"></i>
              </div>
            </div>
            
            <div class="upload-trigger" @click="triggerUpload" v-if="form.images.length < 3">
              <i class="el-icon-plus"></i>
              <span>上传图片</span>
            </div>
          </div>
          <input 
            type="file" 
            ref="fileInput" 
            accept="image/*" 
            multiple 
            class="hidden-input"
            @change="handleFileChange" 
          />
          <div class="upload-tip">支持 jpg, png 格式，最多上传 3 张</div>
        </div>
      </div>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button class="cancel-btn" icon="el-icon-close" @click="handleClose">取消</el-button>
      <el-button class="submit-btn" type="primary" icon="el-icon-check" @click="handleSubmit">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { GOOD_CATEGORIES } from '@/utils/constants'
import { MessageHelper } from '@/utils/messageHelper'

export default {
  name: 'PublishGoodsDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      categories: [],  // 改为空数组，从后端加载
      conditionOptions: [
        { label: '全新', value: '全新', desc: '未使用过，完好如新' },
        { label: '九成新', value: '九成新', desc: '使用次数少，几乎无磨损' },
        { label: '八成新', value: '八成新', desc: '正常使用痕迹，功能完好' },
        { label: '七成新及以下', value: '七成及以下', desc: '使用痕迹明显，但可正常使用' }
      ],
      form: {
        title: '',
        condition: '全新',
        category: '',
        location: '',
        school: '',
        exchangeType: 'pickup',
        exchangeFor: '',
        description: '',
        images: []
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 对话框打开时加载分类
        this.loadCategories();
      }
    }
  },
  methods: {
    async loadCategories() {
      try {
        const response = await this.$axios.get('/categories')
        if (response.code === 20000 && Array.isArray(response.data) && response.data.length > 0) {
          this.categories = response.data
            .filter(cat => cat.isActive === 1)
            .sort((a, b) => a.sortOrder - b.sortOrder)
            .map(cat => ({
              value: cat.categoryId,
              label: cat.categoryName,
              code: cat.categoryCode
            }))
        } else {
          // 接口返回空或格式不对，用常量兜底
          this.categories = GOOD_CATEGORIES.filter(cat => cat.value !== '')
        }
      } catch (error) {
        // 接口不可用，用常量兜底
        this.categories = GOOD_CATEGORIES.filter(cat => cat.value !== '')
      }
    },
    handleClose() {
      this.$emit('update:visible', false)
      this.resetForm()
    },
    resetForm() {
      this.form = {
        title: '',
        condition: 'new',
        category: '',
        location: '',
        school: '',
        exchangeType: 'pickup',
        exchangeFor: '',
        description: '',
        images: []
      }
    },
    triggerUpload() {
      this.$refs.fileInput.click()
    },
    handleFileChange(e) {
      const files = e.target.files
      if (!files.length) return
      
      const remaining = 3 - this.form.images.length
      if (files.length > remaining) {
        MessageHelper.warning(this, `最多只能再上传 ${remaining} 张图片`)
      }
      
      Array.from(files).slice(0, remaining).forEach(file => {
        const reader = new FileReader()
        reader.onload = (e) => {
          this.form.images.push({
            file,
            url: e.target.result
          })
        }
        reader.readAsDataURL(file)
      })
      
      // Reset input value to allow selecting same file again
      e.target.value = ''
    },
    removeImage(index) {
      this.form.images.splice(index, 1)
    },
    async handleSubmit() {
      // 简单校验
      if (!this.form.title || !this.form.category || !this.form.description) {
        MessageHelper.warning(this, '请完善必填信息')
        return
      }
      if (this.form.images.length === 0) {
        MessageHelper.warning(this, '请至少上传一张商品图片')
        return
      }
      
      try {
        MessageHelper.info(this, '正在上传图片...')
        
        // 1. 先上传所有图片到服务器
        const imageUrls = []
        for (let i = 0; i < this.form.images.length; i++) {
          const img = this.form.images[i]
          try {
            // 如果已经是URL（http开头），直接使用
            if (img.url.startsWith('http')) {
              imageUrls.push(img.url)
              continue
            }
            
            // 如果是base64，需要上传
            if (img.url.startsWith('data:')) {
              console.log(`上传第 ${i + 1}/${this.form.images.length} 张图片...`)
              
              const file = this.base64ToFile(img.url, img.file?.name || `image-${i + 1}.jpg`)
              const formData = new FormData()
              formData.append('file', file)
              
              const token = localStorage.getItem('accessToken')
              const uploadResponse = await this.$axios.post(
                '/goods/upload-image',
                formData,
                { headers: { 'Authorization': `Bearer ${token}` } }
              )
              
              if (uploadResponse.code === 20000 && uploadResponse.data) {
                imageUrls.push(uploadResponse.data)
              } else {
                throw new Error(uploadResponse.message || '图片上传失败')
              }
            }
          } catch (uploadError) {
            console.error(`❌ 第 ${i + 1} 张图片上传失败:`, uploadError)
            MessageHelper.error(this, `第 ${i + 1} 张图片上传失败: ${uploadError.message}`)
            return
          }
        }
        
        if (imageUrls.length === 0) {
          MessageHelper.error(this, '没有可用的图片URL')
          return
        }
        
        console.log('✅ 所有图片上传完成，URL列表:', imageUrls)
        
        // 2. 构建请求数据（使用驼峰命名匹配后端）
        const goodsData = {
          goodName: this.form.title,
          description: this.form.description,
          categoryId: parseInt(this.form.category),
          condition: this.form.condition,
          exchangeFor: this.form.exchangeFor || '',
          images: imageUrls
        }
        
        console.log('=== 发布商品 ===')
        console.log('请求数据:', goodsData)
        
        MessageHelper.info(this, '正在发布商品...')
        
        const token = localStorage.getItem('accessToken')
        const response = await this.$axios.post('/goods', goodsData, {
          headers: { Authorization: `Bearer ${token}` }
        })
        
        console.log('=== 发布响应 ===')
        console.log('响应数据:', response)
        
        if (response.code === 20000) {
          MessageHelper.success(this, '发布成功！')
          this.$emit('published', response.data) // 通知父组件刷新列表
          this.handleClose()
          
          // 刷新当前页面的商品列表
          setTimeout(() => {
            window.location.reload()
          }, 500)
        } else {
          MessageHelper.error(this, response.message || '发布失败')
        }
      } catch (error) {
        console.error('=== 发布失败 ===')
        console.error('HTTP状态:', error.response?.status)
        console.error('响应体:', error.response?.data)
        console.error('错误信息:', error.message)
        
        if (error.response?.status === 401) {
          MessageHelper.error(this, '登录已过期，请重新登录')
          setTimeout(() => {
            this.$router.push('/login-page/login')
          }, 1000)
        } else {
          MessageHelper.error(this, error.response?.data?.message || '发布失败，请稍后重试')
        }
      }
    },
    
    // 将base64转换为File对象
    base64ToFile(base64String, filename) {
      const arr = base64String.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new File([u8arr], filename, { type: mime })
    }
  }
}
</script>

<style lang="scss">
// Dialog 全局样式覆盖
.publish-dialog {
  background: var(--card-bg) !important;
  backdrop-filter: blur(var(--glass-blur, 20px));
  border: 1px solid var(--card-border);
  border-radius: 20px !important;
  box-shadow: var(--card-shadow);

  .el-dialog__header {
    padding: 24px 32px;
    border-bottom: none;
    
    .el-dialog__title {
      color: var(--color-text-primary);
      font-size: 24px;
      font-weight: 700;
    }
    
    .el-dialog__headerbtn {
      top: 24px;
      right: 24px;
      font-size: 20px;
      
      .el-dialog__close {
        color: var(--color-text-secondary);
        &:hover { color: var(--color-text-primary); }
      }
    }
  }

  .el-dialog__body {
    padding: 0 32px 32px;
    color: var(--color-text-primary);
  }

  .el-dialog__footer {
    padding: 24px 32px;
    border-top: 1px solid var(--color-border);
  }
}
</style>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: flex;
  gap: 24px;
  .flex-1 { flex: 1; }
}

.form-item {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  
  label {
    width: 80px;
    padding-top: 12px;
    font-size: 14px;
    color: var(--color-text-secondary);
    position: relative;
    
    &.required::before {
      content: '*';
      color: var(--color-secondary-danger);
      margin-right: 4px;
    }
  }
  
  .input-wrapper, .textarea-wrapper, .full-width, .condition-chips, .image-uploader {
    flex: 1;
  }
}

// 通用输入框样式
.input-wrapper {
  position: relative;
  background: var(--color-pill-bg);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 16px;
  transition: all 0.3s ease;
  
  &:hover, &:focus-within {
    background: var(--color-bg-surface);
    border-color: var(--color-primary);
  }
  
  .prefix-icon {
    font-size: 18px;
    color: var(--color-text-disabled);
    margin-right: 12px;
    
    &.gold { color: var(--color-primary); }
  }
  
  input {
    flex: 1;
    height: 48px;
    background: transparent;
    border: none;
    color: var(--color-text-primary);
    font-size: 15px;
    outline: none;
    
    &::placeholder { color: var(--color-text-disabled); }
  }
  
  .counter {
    font-size: 12px;
    color: var(--color-text-disabled);
    margin-left: 12px;
  }
}

// 文本域样式
.textarea-wrapper {
  @extend .input-wrapper;
  padding: 16px;
  align-items: flex-start;
  
  textarea {
    flex: 1;
    background: transparent;
    border: none;
    color: var(--color-text-primary);
    font-size: 15px;
    outline: none;
    resize: none;
    font-family: inherit;
    
    &::placeholder { color: var(--color-text-disabled); }
  }
  
  .counter {
    position: absolute;
    bottom: 12px;
    right: 16px;
  }
}

// 新旧程度卡片
.condition-chips {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  
  .chip-card {
    background: var(--color-pill-bg);
    border: 1px solid var(--color-border);
    border-radius: 12px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    .chip-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--color-text-primary);
      margin-bottom: 6px;
    }
    
    .chip-desc {
      font-size: 12px;
      color: var(--color-text-secondary);
      line-height: 1.4;
    }
    
    &:hover {
      background: var(--color-bg-surface);
    }
    
    &.active {
      background: rgba(var(--color-primary-rgb), 0.15);
      border-color: var(--color-primary);
      
      .chip-title { color: var(--color-primary); }
    }
  }
}

// 图片上传
.image-uploader {
  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .image-preview {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid var(--color-border);
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .delete-mask {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;
      
      i {
        color: white;
        font-size: 20px;
      }
      
      &:hover {
        opacity: 1;
      }
    }
  }

  .upload-trigger {
    width: 100px;
    height: 100px;
    background: var(--color-pill-bg);
    border: 1px dashed var(--color-primary);
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    
    i {
      font-size: 24px;
      color: var(--color-primary);
      margin-bottom: 8px;
    }
    
    span {
      font-size: 12px;
      color: var(--color-primary);
    }
    
    &:hover {
      background: rgba(var(--color-primary-rgb), 0.1);
      transform: translateY(-2px);
    }
  }

  .hidden-input {
    display: none;
  }

  .upload-tip {
    margin-top: 12px;
    font-size: 12px;
    color: var(--color-text-disabled);
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  
  .cancel-btn {
    background: transparent !important;
    border: none !important;
    color: var(--color-text-secondary) !important;
    font-size: 15px;
    
    &:hover { color: var(--color-text-primary) !important; }
  }
  
  .submit-btn {
    background: var(--color-primary) !important;
    border-color: var(--color-primary) !important;
    color: var(--color-nav-active-text) !important; /* Assuming nav active text is good for primary bg */
    padding: 12px 32px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.3);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 28px rgba(var(--color-primary-rgb), 0.4);
    }
  }
}
</style>
