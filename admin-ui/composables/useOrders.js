/**
 * 订单管理 Composable V3.0
 * 封装订单管理的业务逻辑
 * 
 * @module composables/useOrders
 * @description 提供订单管理相关的业务逻辑封装，包括筛选、排序、统计和操作
 * @author 系统管理员
 * @date 2026-03-07
 */
import { OrderFilter, OrderSorter, OrderStats, OrderOperations } from '@/utils/orderManager';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';
import { ORDER_STATUS, ORDER_SORT_OPTIONS } from '@/utils/orderConstants';
import { isSuccess } from '@/composables/useApi';

/**
 * 订单管理组合式函数
 * @param {Object} vm - Vue 组件实例
 * @param {Array} orderList - 订单列表数据
 * @returns {Object} 订单管理方法集合
 */
export function useOrderManagement(vm, orderList) {
  return {
    /**
     * 规范化后端订单数据
     * @param {Object} raw - 后端返回的原始数据
     * @returns {Object} 前端使用的规范化数据
     */
    normalizeOrder(raw) {
      // 如果已经是规范格式，直接返回
      if (raw.orderNo && raw.buyer) return raw;

      // 调试：打印原始数据中的确认字段（已注释）
      // console.log('Raw order data - initiator_confirmed:', raw.initiatorConfirmed, raw.initiator_confirmed);
      // console.log('Raw order data - receiver_confirmed:', raw.receiverConfirmed, raw.receiver_confirmed);

      const order = {
        id: raw.id || raw.order_id,
        orderNo: raw.orderNo || raw.order_number,
        status: raw.status,
        createTime: raw.createTime || raw.created_at,
        remark: raw.remark,
        adminNote: raw.adminNote || raw.admin_note,
        cancelReason: raw.cancelReason || raw.cancel_reason,

        // 双方凭证上传状态（0=未上传，1=已上传）
        // 支持多种字段名格式
        initiator_confirmed: raw.initiatorConfirmed ?? raw.initiator_confirmed ?? raw.InitiatorConfirmed ?? 0,
        receiver_confirmed: raw.receiverConfirmed ?? raw.receiver_confirmed ?? raw.ReceiverConfirmed ?? 0,

        // 发起方评价
        initiatorReview: raw.initiatorReview || raw.initiator_review || '',
        initiatorReviewImages: [],
        initiatorReviewedAt: raw.initiatorReviewedAt || raw.initiator_reviewed_at || null,

        // 接收方评价
        receiverReview: raw.receiverReview || raw.receiver_review || '',
        receiverReviewImages: [],
        receiverReviewedAt: raw.receiverReviewedAt || raw.receiver_reviewed_at || null,

        // 映射买家 (发起方)
        buyer: {
          id: raw.buyerId || raw.initiator_id,
          name: raw.buyerName || '未知用户',
          realName: raw.buyerRealName || '',
          avatar: raw.buyerAvatar || '',
          phone: raw.buyerPhone || '',
          address: raw.buyerAddress || '',
          school: raw.buyerSchool || ''
        },

        // 映射卖家 (接收方)
        seller: {
          id: raw.sellerId || raw.receiver_id,
          name: raw.sellerName || '未知用户',
          realName: raw.sellerRealName || '',
          avatar: raw.sellerAvatar || '',
          phone: raw.sellerPhone || '',
          address: raw.sellerAddress || '',
          school: raw.sellerSchool || ''
        },

        // 映射买家物品 (发起方提供的物品)
        buyerItem: {
          id: raw.buyerItemId || raw.initiator_good_id,
          name: raw.buyerItemName || '暂无物品',
          previewImages: []
        },

        // 映射卖家物品 (接收方提供的物品)
        sellerItem: {
          id: raw.sellerItemId || raw.receiver_good_id,
          name: raw.sellerItemName || '暂无物品',
          previewImages: []
        }
      };

      // 处理图片
      const processImages = (source) => {
        if (!source) return [];
        if (Array.isArray(source)) return source;
        try {
          if (typeof source === 'string') {
             if (source.startsWith('[')) return JSON.parse(source);
             return source.split(',');
          }
        } catch(e) { return []; }
        return [];
      };

      // 处理买家物品图片
      if (raw.buyerItemImages) {
        order.buyerItem.previewImages = processImages(raw.buyerItemImages);
      }

      // 处理卖家物品图片
      if (raw.sellerItemImages) {
        order.sellerItem.previewImages = processImages(raw.sellerItemImages);
      }

      // 处理发起方评价图片
      if (raw.initiatorReviewImages || raw.initiator_review_images) {
        order.initiatorReviewImages = processImages(raw.initiatorReviewImages || raw.initiator_review_images);
      }

      // 处理接收方评价图片
      if (raw.receiverReviewImages || raw.receiver_review_images) {
        order.receiverReviewImages = processImages(raw.receiverReviewImages || raw.receiver_review_images);
      }

      // 处理交换凭证
      order.exchangeProofs = {
        buyer: { uploaded: false, images: [], comment: '' },
        seller: { uploaded: false, images: [], comment: '' }
      };

      if (raw.exchangeProofs && Array.isArray(raw.exchangeProofs)) {
        raw.exchangeProofs.forEach(p => {
          const side = p.userRole === 'initiator' ? 'buyer' : 'seller';
          order.exchangeProofs[side] = {
            uploaded: p.status >= 1,
            images: processImages(p.images),
            comment: p.comment || ''
          };
        });
      }

      return order;
    },

    /**
     * 获取订单统计数据
     * @returns {Object} 包含各状态订单数量的统计对象
     * @example
     * const stats = orderManager.getStats();
     * // { pending: 2, completed: 5, toProcess: 1, cancelled: 3 }
     */
    getStats() {
      return OrderStats.calculate(orderList);
    },

    /**
     * 获取筛选和排序后的订单列表
     * @param {string} filterStatus - 筛选状态
     * @param {string} searchQuery - 搜索关键词
     * @param {string} sortBy - 排序方式
     * @returns {Array} 处理后的订单列表
     */
    getFilteredOrders(filterStatus, searchQuery, sortBy) {
      const filtered = OrderFilter.filter(orderList, filterStatus, searchQuery);
      return OrderSorter.sort(filtered, sortBy);
    },

    /**
     * 查看订单详情
     * @param {Object} order - 订单对象
     * @param {Function} callback - 回调函数
     */
    handleViewDetail(order, callback) {
      if (!order) {
        MessageHelper.warning(vm, '订单数据不存在');
        return;
      }
      callback(order);
    },

    /**
     * 上传实物交换图
     * @param {Object} order - 订单对象
     * @param {string} side - 上传方 ('buyer' 或 'seller')
     * @param {Array} images - 图片URL数组
     * @returns {boolean} 是否上传成功
     */
    handleUploadProof(order, side, images) {
      try {
        OrderOperations.uploadProof(order, side, images);
        
        // 检查是否自动完成
        if (order.status === ORDER_STATUS.COMPLETED) {
          MessageHelper.success(vm, '双方实物图已上传，订单自动完成！');
        } else {
          MessageHelper.success(vm, '实物图上传成功');
        }
        return true;
      } catch (error) {
        console.error('上传实物图失败:', error);
        MessageHelper.error(vm, '上传失败，请重试');
        return false;
      }
    },

    /**
     * 取消订单（从待交易状态）
     * @param {Object} order - 订单对象
     * @param {string} reason - 取消原因
     * @returns {Promise<boolean>} 是否取消成功
     */
    async handleCancelFromPending(order, reason) {
      // 验证取消原因
      if (!reason || reason.trim() === '') {
        MessageHelper.warning(vm, '请填写取消原因');
        return false;
      }

      try {
        const res = await vm.$axios.put(`/api/admin/orders/${order.id}/status`, {
          status: ORDER_STATUS.CANCELLED,
          cancelReason: reason
        });
        
        if (isSuccess(res)) {
          MessageHelper.success(vm, '订单已取消');
          await vm.fetchOrders();
          return true;
        } else {
          MessageHelper.error(vm, res.message || '取消失败');
          return false;
        }
      } catch (error) {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
        return false;
      }
    },

    /**
     * 开始处理待处理订单 - 填写处理方案，转为处理中
     * @param {Object} order - 订单对象
     * @param {string} adminNote - 处理方案
     * @returns {Promise<boolean>} 是否处理成功
     */
    async handleStartProcessing(order, adminNote) {
      try {
        const res = await vm.$axios.put(`/api/admin/orders/${order.id}/status`, {
          status: ORDER_STATUS.PROCESSING,
          adminNote: adminNote
        });
        
        if (isSuccess(res)) {
          MessageHelper.success(vm, '处理方案已保存，订单进入处理中');
          await vm.fetchOrders();
          return true;
        } else {
          MessageHelper.error(vm, res.message || '操作失败');
          return false;
        }
      } catch (error) {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
        return false;
      }
    },

    /**
     * 完成处理 - 从处理中转为已完成
     * @param {Object} order - 订单对象
     * @returns {Promise<boolean>} 是否处理成功
     */
    async handleCompleteProcessing(order) {
      try {
        const res = await vm.$axios.put(`/api/admin/orders/${order.id}/status`, {
          status: ORDER_STATUS.COMPLETED
        });
        
        if (isSuccess(res)) {
          MessageHelper.success(vm, '订单处理完成');
          await vm.fetchOrders();
          return true;
        } else {
          MessageHelper.error(vm, res.message || '操作失败');
          return false;
        }
      } catch (error) {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
        return false;
      }
    },

    /**
     * 处理待处理订单 - 标记为已取消
     * @param {Object} order - 订单对象
     * @param {string} adminNote - 管理员备注（必填）
     * @returns {Promise<boolean>} 是否处理成功
     */
    async handleMarkAsCancelled(order, adminNote) {
      try {
        const res = await vm.$axios.put(`/api/admin/orders/${order.id}/status`, {
          status: ORDER_STATUS.CANCELLED,
          adminNote: adminNote
        });
        
        if (isSuccess(res)) {
          MessageHelper.success(vm, '订单已标记为取消');
          await vm.fetchOrders();
          return true;
        } else {
          MessageHelper.error(vm, res.message || '操作失败');
          return false;
        }
      } catch (error) {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
        return false;
      }
    },

    /**
     * 将已完成订单改为待处理
     * @param {Object} order - 订单对象
     * @returns {Promise<boolean>} 是否转换成功
     */
    async handleMoveToProcess(order) {
      try {
        const res = await vm.$axios.put(`/api/admin/orders/${order.id}/status`, {
          status: ORDER_STATUS.TO_PROCESS
        });
        
        if (isSuccess(res)) {
          MessageHelper.success(vm, '订单已转为待处理');
          await vm.fetchOrders();
          return true;
        } else {
          MessageHelper.error(vm, res.message || '操作失败');
          return false;
        }
      } catch (error) {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
        return false;
      }
    },

    /**
     * 删除订单
     * @param {Object} order - 要删除的订单对象
     * @returns {Promise<void>}
     */
    async handleDelete(order) {
      try {
        await ConfirmHelper.confirmDelete(vm, `订单 ${order.orderNo}`);
        
        const res = await vm.$axios.delete(`/api/admin/orders/${order.id}`);
        if (isSuccess(res)) {
          MessageHelper.success(vm, '订单已删除');
          await vm.fetchOrders();
        } else {
          MessageHelper.error(vm, res.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(vm, '操作失败');
        }
      }
    },

    /**
     * 清除所有筛选条件
     * @param {Function} callback - 回调函数，接收重置后的筛选条件
     */
    clearFilters(callback) {
      callback({
        filterStatus: ORDER_STATUS.ALL,
        searchQuery: '',
        sortBy: ORDER_SORT_OPTIONS.DEFAULT
      });
    }
  };
}
