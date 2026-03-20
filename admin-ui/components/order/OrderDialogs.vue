<template>
  <div>
    <!-- 订单详情弹窗 -->
    <el-dialog
      title="订单详情"
      :visible="detailVisible"
      @update:visible="$emit('update:detailVisible', $event)"
      width="750px"
      custom-class="glass-dialog order-dialog"
      top="5vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <div v-if="currentOrder" class="order-detail-compact">
        <!-- 交换双方信息 - 紧凑布局 -->
        <div class="exchange-parties-compact">
          <!-- 换物方A -->
          <div class="party-card" :class="{ 'completed': currentOrder.initiator_confirmed === 1 || currentOrder.status === 'completed' }">
            <div class="party-info-container">
              <div :class="['party-avatar', getAvatarClass(currentOrder.buyer?.id)]">
                <span v-if="!currentOrder.buyer?.avatar">{{ getInitials(currentOrder.buyer?.name) }}</span>
                <img v-else :src="currentOrder.buyer.avatar" :alt="currentOrder.buyer?.name" />
              </div>
              <div class="party-details-wrapper" v-if="currentOrder.buyer">
                <div class="party-name">{{ currentOrder.buyer.name }}</div>
                <div class="party-id">ID: {{ currentOrder.buyer.id || '-' }}</div>
                <div class="contact-item" v-if="currentOrder.buyer.realName">
                  <i class="el-icon-user"></i>
                  <span>{{ currentOrder.buyer.realName }}</span>
                </div>
                <div class="contact-item">
                  <i class="el-icon-phone"></i>
                  <span>{{ currentOrder.buyer.phone || '-' }}</span>
                </div>
                <div class="contact-item" v-if="currentOrder.buyer.school">
                  <i class="el-icon-school"></i>
                  <span>{{ currentOrder.buyer.school }}</span>
                </div>
                <div class="contact-item">
                  <i class="el-icon-location"></i>
                  <span>{{ currentOrder.buyer.address || '-' }}</span>
                </div>
              </div>
            </div>

            <div class="party-item-info" v-if="currentOrder.buyerItem">
              <div class="item-label">| 提供物品</div>
              <div class="item-card">
                <div class="item-images-grid" v-if="currentOrder.buyerItem.previewImages">
                  <div 
                    v-for="(img, idx) in currentOrder.buyerItem.previewImages.slice(0, 1)" 
                    :key="idx"
                    class="item-image-thumb"
                    @click="previewImage(img)"
                  >
                    <img :src="img" :alt="`${currentOrder.buyerItem.name} ${idx + 1}`" />
                  </div>
                </div>
                <div class="item-details">
                  <div class="item-name">{{ currentOrder.buyerItem.name }}</div>
                </div>
              </div>
            </div>

            <div class="proof-section-mini">
              <div class="section-label">
                <i class="el-icon-picture"></i>
                <span>收到实物图</span>
              </div>
              <div class="proof-images-row" v-if="currentOrder.receiverReviewImages && currentOrder.receiverReviewImages.length > 0">
                <div
                  v-for="(img, idx) in currentOrder.receiverReviewImages.slice(0, 3)"
                  :key="idx"
                  class="proof-thumb-mini"
                >
                  <img :src="img" @click="previewImage(img)" />
                </div>
              </div>
              <div class="proof-empty-mini" v-else>暂无</div>

              <!-- 评价内容 -->
              <div class="review-section-mini">
                <div class="section-label">
                  <i class="el-icon-chat-dot-round"></i>
                  <span>评价内容</span>
                </div>
                <div class="review-content" v-if="currentOrder.initiatorReview">
                  <p>{{ currentOrder.initiatorReview }}</p>
                </div>
                <div class="review-empty" v-else>暂无评价</div>
              </div>

              <!-- 凭证上传状态 -->
              <div class="upload-confirm-status">
                <div class="status-badge" :class="currentOrder.initiator_confirmed === 1 || currentOrder.status === 'completed' ? 'confirmed' : 'pending'">
                  <i :class="currentOrder.initiator_confirmed === 1 || currentOrder.status === 'completed' ? 'el-icon-check' : 'el-icon-warning-outline'"></i>
                  <span>{{ currentOrder.initiator_confirmed === 1 || currentOrder.status === 'completed' ? '已上传凭证' : '未上传凭证' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 交换箭头 -->
          <div class="exchange-icon">
            <i class="el-icon-sort"></i>
          </div>

          <!-- 换物方B -->
          <div class="party-card" :class="{ 'completed': currentOrder.receiver_confirmed === 1 || currentOrder.status === 'completed' }">
            <div class="party-info-container">
              <div :class="['party-avatar', getAvatarClass(currentOrder.seller?.id)]">
                <span v-if="!currentOrder.seller?.avatar">{{ getInitials(currentOrder.seller?.name) }}</span>
                <img v-else :src="currentOrder.seller.avatar" :alt="currentOrder.seller?.name" />
              </div>
              <div class="party-details-wrapper" v-if="currentOrder.seller">
                <div class="party-name">{{ currentOrder.seller.name }}</div>
                <div class="party-id">ID: {{ currentOrder.seller.id || '-' }}</div>
                <div class="contact-item" v-if="currentOrder.seller.realName">
                  <i class="el-icon-user"></i>
                  <span>{{ currentOrder.seller.realName }}</span>
                </div>
                <div class="contact-item">
                  <i class="el-icon-phone"></i>
                  <span>{{ currentOrder.seller.phone || '-' }}</span>
                </div>
                <div class="contact-item" v-if="currentOrder.seller.school">
                  <i class="el-icon-school"></i>
                  <span>{{ currentOrder.seller.school }}</span>
                </div>
                <div class="contact-item">
                  <i class="el-icon-location"></i>
                  <span>{{ currentOrder.seller.address || '-' }}</span>
                </div>
              </div>
            </div>

            <div class="party-item-info" v-if="currentOrder.sellerItem">
              <div class="item-label">| 提供物品</div>
              <div class="item-card">
                <div class="item-images-grid" v-if="currentOrder.sellerItem.previewImages">
                  <div 
                    v-for="(img, idx) in currentOrder.sellerItem.previewImages.slice(0, 1)" 
                    :key="idx"
                    class="item-image-thumb"
                    @click="previewImage(img)"
                  >
                    <img :src="img" :alt="`${currentOrder.sellerItem.name} ${idx + 1}`" />
                  </div>
                </div>
                <div class="item-details">
                  <div class="item-name">{{ currentOrder.sellerItem.name }}</div>
                </div>
              </div>
            </div>

            <div class="proof-section-mini">
              <div class="section-label">
                <i class="el-icon-picture"></i>
                <span>收到实物图</span>
              </div>
              <div class="proof-images-row" v-if="currentOrder.initiatorReviewImages && currentOrder.initiatorReviewImages.length > 0">
                <div
                  v-for="(img, idx) in currentOrder.initiatorReviewImages.slice(0, 3)"
                  :key="idx"
                  class="proof-thumb-mini"
                >
                  <img :src="img" @click="previewImage(img)" />
                </div>
              </div>
              <div class="proof-empty-mini" v-else>暂无</div>

              <!-- 评价内容 -->
              <div class="review-section-mini">
                <div class="section-label">
                  <i class="el-icon-chat-dot-round"></i>
                  <span>评价内容</span>
                </div>
                <div class="review-content" v-if="currentOrder.receiverReview">
                  <p>{{ currentOrder.receiverReview }}</p>
                </div>
                <div class="review-empty" v-else>暂无评价</div>
              </div>

              <!-- 凭证上传状态 -->
              <div class="upload-confirm-status">
                <div class="status-badge" :class="currentOrder.receiver_confirmed === 1 || currentOrder.status === 'completed' ? 'confirmed' : 'pending'">
                  <i :class="currentOrder.receiver_confirmed === 1 || currentOrder.status === 'completed' ? 'el-icon-check' : 'el-icon-warning-outline'"></i>
                  <span>{{ currentOrder.receiver_confirmed === 1 || currentOrder.status === 'completed' ? '已上传凭证' : '未上传凭证' }}</span>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- 管理员备注 - 放在两个卡片下方 -->
        <div class="audit-section-bottom">
          <div class="section-label">
            <i class="el-icon-document-checked"></i>
            <span>管理员备注</span>
          </div>
          <el-input
            v-model="editableAdminNote"
            type="textarea"
            :rows="2"
            placeholder="输入管理员备注..."
            maxlength="200"
            show-word-limit
            class="glass-input"
          ></el-input>
          <button class="glass-btn btn-save" @click="saveAdminNote">
            <i class="el-icon-check"></i>
            <span>保存</span>
          </button>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝退换货弹窗 -->
    <el-dialog
      title="拒绝退换货"
      :visible="rejectReturnExchangeVisible"
      @update:visible="$emit('update:rejectReturnExchangeVisible', $event)"
      width="500px"
      custom-class="glass-dialog"
      top="15vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px" label-position="left">
        <el-form-item label="拒绝原因">
          <el-input
            type="textarea"
            :value="rejectReason"
            @input="$emit('update:rejectReason', $event)"
            :rows="4"
            placeholder="请输入拒绝退换货的原因"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:rejectReturnExchangeVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-save" @click="$emit('confirm-reject-return-exchange')">
          <i class="el-icon-check"></i>
          <span>确认拒绝</span>
        </button>
      </span>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog
      title="取消订单"
      :visible="cancelVisible"
      @update:visible="$emit('update:cancelVisible', $event)"
      width="500px"
      custom-class="glass-dialog cancel-dialog"
      top="15vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="dialog-icon warning-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="dialog-desc">
          请输入取消订单的原因，以便我们改进服务
        </div>
        <el-form label-width="0">
          <el-form-item>
            <el-input
              type="textarea"
              :value="cancelReason"
              @input="$emit('update:cancelReason', $event)"
              :rows="4"
              placeholder="请输入取消订单的原因..."
              maxlength="200"
              show-word-limit
              class="glass-textarea"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:cancelVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-danger" @click="$emit('confirm-cancel')">
          <i class="el-icon-check"></i>
          <span>确认取消订单</span>
        </button>
      </span>
    </el-dialog>

    <!-- 处理订单弹窗 -->
    <el-dialog
      title="处理订单"
      :visible="markCompletedVisible"
      @update:visible="$emit('update:markCompletedVisible', $event)"
      width="500px"
      custom-class="glass-dialog complete-dialog"
      top="15vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="dialog-icon success-icon">
          <i class="el-icon-s-claim"></i>
        </div>
        <div class="dialog-desc">
          请填写处理方案，保存后订单将转为已完成状态
        </div>
        <el-form label-width="0">
          <el-form-item>
            <el-input
              type="textarea"
              :value="adminNote"
              @input="$emit('update:adminNote', $event)"
              :rows="4"
              placeholder="请输入处理方案（必填）..."
              maxlength="200"
              show-word-limit
              class="glass-textarea"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:markCompletedVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-success" @click="$emit('confirm-mark-completed')">
          <i class="el-icon-check"></i>
          <span>保存</span>
        </button>
      </span>
    </el-dialog>

    <!-- 处理订单弹窗 - 标记为取消 -->
    <el-dialog
      title="标记订单为取消"
      :visible="markCancelledVisible"
      @update:visible="$emit('update:markCancelledVisible', $event)"
      width="500px"
      custom-class="glass-dialog cancel-dialog"
      top="15vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="dialog-icon warning-icon">
          <i class="el-icon-circle-close"></i>
        </div>
        <div class="dialog-desc">
          确认将此订单标记为已取消状态？请填写处理备注
        </div>
        <el-form label-width="0">
          <el-form-item>
            <el-input
              type="textarea"
              :value="adminNote"
              @input="$emit('update:adminNote', $event)"
              :rows="4"
              placeholder="请输入处理备注（必填）..."
              maxlength="200"
              show-word-limit
              class="glass-textarea"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:markCancelledVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-danger" @click="$emit('confirm-mark-cancelled')">
          <i class="el-icon-check"></i>
          <span>确认标记取消</span>
        </button>
      </span>
    </el-dialog>

    <!-- 验证凭证弹窗 -->
    <el-dialog
      :title="verifyProofTitle"
      :visible="verifyProofVisible"
      @update:visible="$emit('update:verifyProofVisible', $event)"
      width="700px"
      custom-class="glass-dialog"
      top="10vh"
      :modal="false"
      :close-on-click-modal="false"
    >
      <div v-if="currentProofData" class="proof-verify-content">
        <div class="proof-info">
          <div class="info-row">
            <span class="label">上传时间:</span>
            <span class="value">{{ formatFullTime(currentProofData.uploadTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">上传状态:</span>
            <el-tag :type="getProofStatusType(currentProofData.status)" size="small">
              {{ getProofStatusLabel(currentProofData.status) }}
            </el-tag>
          </div>
        </div>
        
        <div class="proof-images-preview">
          <div class="images-title">物品凭证图片</div>
          <div class="images-grid">
            <div 
              v-for="(img, idx) in currentProofData.images" 
              :key="idx" 
              class="image-item"
              @click="previewImage(img)"
            >
              <img :src="img" :alt="`凭证图片${idx + 1}`" />
              <div class="image-overlay">
                <i class="el-icon-zoom-in"></i>
              </div>
            </div>
          </div>
          <div v-if="currentProofData.images.length === 0" class="no-images">
            暂无上传图片
          </div>
        </div>

        <div class="verify-tips">
          <i class="el-icon-warning"></i>
          <span>请仔细核对物品凭证图片，确认物品真实性和成色后再验证通过</span>
        </div>
      </div>
      <span slot="footer">
        <button class="glass-btn btn-cancel" @click="$emit('update:verifyProofVisible', false)">
          <i class="el-icon-close"></i>
          <span>取消</span>
        </button>
        <button class="glass-btn btn-save" @click="$emit('confirm-verify-proof')">
          <i class="el-icon-check"></i>
          <span>验证通过</span>
        </button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { ORDER_STATUS_LABELS, PAYMENT_METHOD_LABELS, SHIPPING_METHOD_LABELS } from '@/utils/orderConstants';

export default {
  name: 'OrderDialogs',
  props: {
    detailVisible: Boolean,
    rejectReturnExchangeVisible: Boolean,
    cancelVisible: Boolean,
    verifyProofVisible: Boolean,
    markCompletedVisible: Boolean,
    markCancelledVisible: Boolean,
    currentOrder: Object,
    currentProofData: Object,
    verifyProofSide: String,
    rejectReason: String,
    cancelReason: String,
    adminNote: String
  },
  data() {
    return {
      editableRemark: '',
      editableAdminNote: ''
    };
  },
  watch: {
    currentOrder: {
      handler(newVal) {
        if (newVal) {
          this.editableRemark = newVal.remark || '';
          this.editableAdminNote = newVal.adminNote || '';
        }
      },
      immediate: true
    }
  },
  computed: {
    verifyProofTitle() {
      if (this.verifyProofSide === 'buyer') {
        return '验证换物方A凭证';
      } else if (this.verifyProofSide === 'seller') {
        return '验证换物方B凭证';
      }
      return '验证凭证';
    }
  },
  methods: {
    saveRemark() {
      if (this.currentOrder) {
        this.$emit('update-remark', {
          orderId: this.currentOrder.id,
          remark: this.editableRemark
        });
        this.$root.$emit('show-island-message', '备注已保存', 'success');
      }
    },

    saveAdminNote() {
      if (this.currentOrder) {
        this.$emit('update-admin-note', {
          orderId: this.currentOrder.id,
          adminNote: this.editableAdminNote
        });
        this.$root.$emit('show-island-message', '管理员备注已保存', 'success');
      }
    },
    
    showFullDetail() {
      this.$message.info('查看完整订单详情');
    },
    
    formatRelativeTime(timestamp) {
      if (!timestamp) return '-';
      const now = Date.now();
      const diff = now - timestamp;
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 1) return '刚刚';
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      
      return this.formatFullTime(timestamp);
    },
    
    previewImage(url) {
      // 使用浏览器原生方式预览图片
      window.open(url, '_blank');
    },
    
    getProofStatusLabel(status) {
      const labels = {
        'not_uploaded': '未上传',
        'uploaded': '已上传',
        'verified': '已验证'
      };
      return labels[status] || status;
    },
    
    getProofStatusType(status) {
      const typeMap = {
        'not_uploaded': 'info',
        'uploaded': 'warning',
        'verified': 'success'
      };
      return typeMap[status] || 'info';
    },
    getStatusLabel(status) {
      return ORDER_STATUS_LABELS[status] || status;
    },
    
    getStatusType(status) {
      const typeMap = {
        'pending_payment': 'warning',
        'pending_shipment': 'info',
        'shipped': 'primary',
        'completed': 'success',
        'cancelled': 'info',
        'refunding': 'warning',
        'refunded': 'info'
      };
      return typeMap[status] || 'info';
    },
    
    getPaymentMethodLabel(method) {
      return PAYMENT_METHOD_LABELS[method] || method;
    },
    
    getShippingMethodLabel(method) {
      return SHIPPING_METHOD_LABELS[method] || method;
    },
    
    formatFullTime(timestamp) {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    maskPhone(phone) {
      if (!phone) return '-';
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
    },

    getInitials(name) {
      if (!name) return '??';
      return name.substring(0, 2).toUpperCase();
    },

    getAvatarClass(id) {
      if (!id) return 'avatar-default';
      const colors = ['red-avatar', 'pink-avatar', 'cyan-avatar', 'orange-avatar', 'purple-avatar', 'blue-avatar'];
      const index = String(id).charCodeAt(0) % colors.length;
      return colors[index];
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

// 紧凑型订单详情样式
.order-detail-compact {
  padding-bottom: 20px; // 确保底部内容不被遮挡

  .exchange-parties-compact {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    gap: 30px;
    margin-bottom: 0;
    
    .party-card {
      // Make background transparent as per image
      background: transparent;
      border-radius: 12px;
      padding: 20px;
      border: 2px solid transparent;
      display: flex;
      flex-direction: column;
      gap: 20px;
      transition: all 0.3s ease;

      &.completed {
        border-color: rgba(var(--color-secondary-success-rgb), 0.6);
        background: rgba(var(--color-secondary-success-rgb), 0.05);
        box-shadow: 0 0 20px rgba(var(--color-secondary-success-rgb), 0.3);
      }
      
      .party-info-container {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        padding-bottom: 0;
        border-bottom: none;
        margin-bottom: 0;
        
        .party-avatar {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 16px;
          color: #fff;
          overflow: hidden;
          flex-shrink: 0;
          box-shadow: 0 4px 12px rgba(0,0,0,0.3);
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          // Same colors as OrderList
          &.red-avatar { background: #ff4d4f; }
          &.pink-avatar { background: #ff4081; }
          &.cyan-avatar { background: #13c2c2; }
          &.orange-avatar { background: #fa8c16; }
          &.purple-avatar { background: #722ed1; }
          &.blue-avatar { background: #1890ff; }
          &.avatar-default { background: #8c8c8c; }
        }
        
        .party-details-wrapper {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 4px;
          
          .party-name {
            font-size: 16px;
            font-weight: bold;
            color: $color-text-primary;
          }
          
          .party-id {
            font-size: 12px;
            color: $color-text-secondary;
            font-family: 'Courier New', monospace;
          }
          
          .contact-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: $color-text-primary;
            
            i {
              font-size: 13px;
              color: $color-primary;
              flex-shrink: 0;
            }
          }
        }
      }
      
      .party-item-info {
        .item-label {
          font-size: 13px;
          font-weight: 500;
          color: $color-text-primary;
          margin-bottom: 12px;
          padding-left: 0;
          border-left: 2px solid $color-primary;
          padding-left: 8px;
        }
        
        .item-card {
          background: transparent;
          border-radius: 0;
          padding: 0;
          
          .item-images-grid {
            display: block;
            margin-bottom: 12px;
            
            .item-image-thumb {
              width: 120px;
              height: 120px;
              border-radius: 12px;
              overflow: hidden;
              background: #000;
              cursor: pointer;
              border: none;
              box-shadow: 0 4px 12px rgba(0,0,0,0.3);
              
              img {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }
            }
          }
          
          .item-details {
            .item-name {
              font-size: 15px;
              font-weight: bold;
              color: $color-text-primary;
              margin-bottom: 4px;
            }
            
            .item-desc {
              font-size: 12px;
              color: $color-text-secondary;
            }
          }
        }
      }

      .proof-section-mini {
        .section-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          font-weight: 500;
          color: $color-text-primary;
          margin-bottom: 12px;

          i {
            color: #ff4081;
          }
        }

        .proof-images-row {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }

        .proof-thumb-mini {
          width: 100px;
          height: 100px;
          border-radius: 12px;
          overflow: hidden;
          background: #000;
          cursor: pointer;
          box-shadow: 0 4px 12px rgba(0,0,0,0.3);

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .proof-empty-mini {
          width: 100px;
          height: 100px;
          border-radius: 12px;
          background: rgba(255,255,255,0.05);
          display: flex;
          align-items: center;
          justify-content: center;
          color: $color-text-secondary;
          font-size: 12px;
        }

        .proof-comment {
          margin-top: 10px;
          display: flex;
          align-items: flex-start;
          gap: 6px;
          padding: 8px 12px;
          background: rgba(255,255,255,0.05);
          border-radius: 8px;
          border-left: 2px solid $color-primary;

          i {
            color: $color-primary;
            font-size: 14px;
            margin-top: 2px;
            flex-shrink: 0;
          }

          span {
            font-size: 12px;
            color: $color-text-primary;
            line-height: 1.5;
            word-break: break-word;
          }
        }

        .review-section-mini {
          margin-top: 16px;

          .section-label {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            font-weight: 500;
            color: $color-text-primary;
            margin-bottom: 12px;

            i {
              color: $color-primary;
            }
          }

          .review-content {
            padding: 12px;
            background: rgba(255,255,255,0.05);
            border-radius: 8px;
            border-left: 2px solid $color-primary;

            p {
              font-size: 13px;
              color: $color-text-primary;
              line-height: 1.6;
              margin: 0 0 10px 0;
              word-break: break-word;
            }

            .review-images {
              display: flex;
              gap: 8px;
              flex-wrap: wrap;

              .review-thumb {
                width: 80px;
                height: 80px;
                border-radius: 8px;
                overflow: hidden;
                background: #000;
                cursor: pointer;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);

                img {
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                }
              }
            }
          }

          .review-empty {
            padding: 12px;
            background: rgba(255,255,255,0.03);
            border-radius: 8px;
            color: $color-text-secondary;
            font-size: 12px;
            text-align: center;
          }
        }

        .upload-confirm-status {
          margin-top: 10px;

          .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 500;

            &.confirmed {
              background: rgba(var(--color-secondary-success-rgb), 0.15);
              color: $color-secondary-success;
              border: 1px solid rgba(var(--color-secondary-success-rgb), 0.3);
            }

            &.pending {
              background: rgba(var(--color-secondary-warning-rgb), 0.15);
              color: $color-secondary-warning;
              border: 1px solid rgba(var(--color-secondary-warning-rgb), 0.3);
            }

            i {
              font-size: 12px;
            }
          }
        }
      }

      .audit-section {
        margin-top: 10px;
        .section-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          font-weight: 500;
          color: $color-text-primary;
          margin-bottom: 12px;

          i { color: $color-text-secondary; }
        }
      }

      .audit-section-bottom {
        margin-top: 24px;
        padding: 20px;
        background: linear-gradient(135deg,
          rgba(255, 255, 255, 0.08),
          rgba(255, 255, 255, 0.04));
        backdrop-filter: blur(10px);
        border-radius: 16px;
        border: 1px solid rgba($color-border, 0.2);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

        .section-label {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          font-weight: 600;
          color: $color-text-primary;
          margin-bottom: 16px;

          i {
            color: $color-primary;
            font-size: 16px;
          }
        }
      }

      .remark-footer {
        margin-top: 20px;
        .save-remark-btn {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 10px 24px;
          background: $color-primary; // Use solid primary color
          border: none;
          border-radius: 20px; // Rounded pill shape
          color: #fff;
          font-size: 14px;
          font-weight: bold;
          cursor: pointer;
          box-shadow: 0 4px 15px rgba(var(--color-primary-rgb), 0.4);
          
          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.6);
          }
        }
      }

      .remark-input-area {
        margin-top: auto;
        display: flex;
        justify-content: flex-end;
        align-items: flex-end;
        position: relative;
        
        .hidden-textarea {
          // Visually hidden but accessible if needed, or transparent
          opacity: 0; 
          height: 0;
          overflow: hidden;
        }
        
        .char-count {
          font-size: 12px;
          color: $color-text-secondary;
          margin-top: 20px;
        }
      }
    }
    
    .exchange-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      padding-top: 100px; // Push down to align with item images roughly
      
      i {
        font-size: 24px;
        color: $color-primary; // Cyan color
      }
    }
  }
}

.glass-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.btn-cancel {
    background: rgba($color-text-secondary, 0.1);
    border: 1px solid rgba($color-border, 0.3);
    color: $color-text-secondary;

    &:hover {
      background: rgba($color-text-secondary, 0.15);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba($color-text-secondary, 0.2);
    }
  }

  &.btn-save,
  &.btn-success {
    background: linear-gradient(135deg,
      rgba(var(--color-secondary-success-rgb), 0.95),
      rgba(var(--color-secondary-success-rgb), 0.85));
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-secondary-success-rgb), 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-secondary-success-rgb), 0.4);
    }
  }

  &.btn-danger {
    background: linear-gradient(135deg,
      rgba(var(--color-secondary-danger-rgb), 0.95),
      rgba(var(--color-secondary-danger-rgb), 0.85));
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-secondary-danger-rgb), 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-secondary-danger-rgb), 0.4);
    }
  }

  &:active {
    transform: translateY(0);
  }

  i {
    font-size: 15px;
  }
}

.glass-input {
  margin-bottom: 16px;

  ::v-deep .el-textarea__inner {
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.04));
    backdrop-filter: blur(10px);
    border: 1px solid rgba($color-border, 0.2);
    border-radius: 12px;
    color: $color-text-primary;
    font-size: 13px;
    padding: 12px 14px;
    resize: none;
    transition: all 0.3s ease;

    &:focus {
      background: linear-gradient(135deg,
        rgba(255, 255, 255, 0.12),
        rgba(255, 255, 255, 0.06));
      border-color: rgba($color-primary, 0.5);
      box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
    }

    &::placeholder {
      color: rgba($color-text-disabled, 0.6);
    }
  }

  ::v-deep .el-input__count {
    background: transparent;
    color: rgba($color-text-disabled, 0.7);
    font-size: 11px;
  }
}

// 弹窗内容样式
.dialog-content {
  padding: 10px 0;
  
  .dialog-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    margin: 0 auto 20px;
    border-radius: 50%;
    backdrop-filter: blur(8px);
    
    i {
      font-size: 32px;
    }
    
    &.success-icon {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-success-rgb), 0.2), 
        rgba(var(--color-secondary-success-rgb), 0.1));
      box-shadow: 0 4px 16px rgba(var(--color-secondary-success-rgb), 0.25),
                  inset 0 1px 0 rgba(255, 255, 255, 0.2);
      
      i {
        color: $color-secondary-success;
      }
    }
    
    &.warning-icon {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-warning-rgb), 0.2), 
        rgba(var(--color-secondary-warning-rgb), 0.1));
      box-shadow: 0 4px 16px rgba(var(--color-secondary-warning-rgb), 0.25),
                  inset 0 1px 0 rgba(255, 255, 255, 0.2);
      
      i {
        color: $color-secondary-warning;
      }
    }
  }
  
  .dialog-desc {
    text-align: center;
    font-size: 14px;
    color: $color-text-secondary;
    line-height: 1.6;
    margin-bottom: 24px;
    padding: 0 20px;
  }
  
  .el-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
}

// 毛玻璃输入框样式
.glass-textarea {
  ::v-deep .el-textarea__inner {
    padding: 14px 16px;
    background: linear-gradient(135deg, 
      rgba(var(--color-bg-surface), 0.6), 
      rgba(var(--color-bg-surface), 0.4));
    backdrop-filter: blur(10px);
    border: 1px solid rgba($color-border, 0.3);
    border-radius: 12px;
    font-size: 13px;
    color: $color-text-primary;
    line-height: 1.6;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    resize: none;
    
    &:focus {
      background: linear-gradient(135deg, 
        rgba(var(--color-bg-surface), 0.7), 
        rgba(var(--color-bg-surface), 0.5));
      border-color: $color-primary;
      box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1),
                  inset 0 1px 0 rgba(255, 255, 255, 0.1);
    }
    
    &::placeholder {
      color: $color-text-disabled;
    }
  }
  
  ::v-deep .el-input__count {
    background: transparent !important;
    color: $color-text-disabled;
    font-size: 11px;
    right: 12px;
    bottom: 8px;
  }
}

// 弹窗底部按钮区域
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 10px;
}

.proof-verify-content {
    ::v-deep .el-tag {
      border: none;
      border-radius: 20px;
      padding: 0 16px;
      height: 28px;
      line-height: 28px;
      font-weight: 500;
      font-size: 12px;
      
      &.el-tag--success {
        background-color: rgba(var(--color-secondary-success-rgb), 0.15);
        color: var(--color-secondary-success);
      }
      
      &.el-tag--info {
        background-color: rgba(var(--color-secondary-info-rgb), 0.15);
        color: var(--color-secondary-info);
      }
      
      &.el-tag--warning {
        background-color: rgba(var(--color-secondary-warning-rgb), 0.15);
        color: var(--color-secondary-warning);
      }
      
      &.el-tag--danger {
        background-color: rgba(var(--color-secondary-danger-rgb), 0.15);
        color: var(--color-secondary-danger);
      }
    }

    .proof-info {
    padding: 16px;
    background: rgba(var(--color-secondary-info-rgb), 0.05);
    border-radius: 12px;
    margin-bottom: 20px;
    
    .info-row {
      display: flex;
      align-items: center;
      padding: 6px 0;
      
      .label {
        font-size: 13px;
        color: $color-text-secondary;
        min-width: 80px;
      }
      
      .value {
        font-size: 13px;
        color: $color-text-primary;
        font-weight: 500;
      }
    }
  }
  
  .proof-images-preview {
    margin-bottom: 20px;
    
    .images-title {
      font-size: 14px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 12px;
    }
    
    .images-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      
      .image-item {
        position: relative;
        aspect-ratio: 1;
        border-radius: 12px;
        overflow: hidden;
        cursor: pointer;
        border: 2px solid rgba($color-border, 0.3);
        transition: all 0.3s ease;
        
        &:hover {
          border-color: $color-primary;
          transform: scale(1.05);
          
          .image-overlay {
            opacity: 1;
          }
        }
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s ease;
          
          i {
            font-size: 32px;
            color: #fff;
          }
        }
      }
    }
    
    .no-images {
      padding: 40px;
      text-align: center;
      color: $color-text-secondary;
      font-size: 13px;
      background: rgba($color-text-secondary, 0.05);
      border-radius: 12px;
      border: 1px dashed rgba($color-border, 0.3);
    }
  }
  
  .verify-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: rgba(var(--color-secondary-warning-rgb), 0.1);
    border-radius: 10px;
    border-left: 3px solid $color-secondary-warning;
    
    i {
      font-size: 16px;
      color: $color-secondary-warning;
    }
    
    span {
      font-size: 12px;
      color: $color-text-secondary;
      line-height: 1.5;
    }
  }
}
</style>
