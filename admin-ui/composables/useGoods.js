import { GoodsFilter, GoodsSorter, GoodsStats, GoodsOperations } from '@/utils/goodsManager';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';
import { GOOD_STATUS, SORT_OPTIONS, CONDITION_LEVEL_MAP, CATEGORY_MAP } from '@/utils/constants';
import { isSuccess } from '@/composables/useApi';

/**
 * 商品管理 Composable - 完整版
 * 封装所有商品相关的状态和操作
 */
export function useGoodsManagement(vm, initialGoodsList = []) {
  // 响应式状态
  const state = vm.$data || {};
  
  // 初始化商品列表
  if (!state.goodsList) {
    vm.$set(vm, 'goodsList', initialGoodsList);
  }

  /**
   * 规范化后端商品数据
   */
  const normalizeGood = (bg) => {
    const fg = { ...bg };

    // 1. 基础字段映射
    fg.id = bg.id;
    fg.name = bg.name;
    fg.description = bg.description;

    // 2. 分类映射
    // 保留 categoryId 用于编辑表单
    fg.categoryId = bg.categoryId;
    // 优先使用后端返回的 category 名称，如果没有则尝试通过 categoryId 查找
    if (bg.category) {
      fg.category = bg.category;
    } else if (bg.categoryId) {
      const categoryName = Object.keys(CATEGORY_MAP).find(key => CATEGORY_MAP[key] === bg.categoryId);
      if (categoryName) fg.category = categoryName;
    }

    // 3. 成色映射
    // 优先使用后端返回的 condition 描述，如果没有则通过 conditionLevel 查找
    if (bg.condition) {
      // 假设后端返回的是中文描述，这里需要映射回前端的英文 key
      // 这里简化处理，如果后端没返回 key，则通过 level 判断
      // 或者如果后端直接返回 'brand_new' 等 key
      fg.condition = bg.condition;
    }
    if (!fg.condition && bg.conditionLevel) {
      if (bg.conditionLevel >= 10) fg.condition = 'brand_new';
      else if (bg.conditionLevel >= 9) fg.condition = 'like_new';
      else if (bg.conditionLevel >= 8) fg.condition = 'good';
      else fg.condition = 'fair';
    }

    // 4. 状态映射
    // 后端: status (0=待审核, 1=已通过, 2=已拒绝)
    // 后端: shelfStatus (0=已下架, 1=已上架)
    // 后端: soldTime (有值=已售出)
    if (bg.status === 2) {
      fg.status = GOOD_STATUS.REJECTED;
    } else if (bg.status === 0) {
      fg.status = GOOD_STATUS.PENDING;
    } else if (bg.status === 1) {
      if (bg.soldTime) {
        fg.status = GOOD_STATUS.SOLD;
      } else if (bg.shelfStatus === 0) {
        fg.status = GOOD_STATUS.OFF_SHELF;
      } else {
        fg.status = GOOD_STATUS.ACTIVE;
      }
    }

    // 设置开关状态
    fg.isActive = (fg.status === GOOD_STATUS.ACTIVE);

    // 5. 统计数据映射
    fg.views = bg.views || 0;
    fg.likes = bg.likes || 0;
    fg.favorites = bg.favorites || 0;

    // 6. 其他字段
    fg.exchangeCode = bg.exchangeCode;
    fg.rejectReason = bg.rejectReason;
    fg.soldTime = bg.soldTime;
    fg.deliveryType = bg.deliveryType;

    // 7. 审核相关字段
    fg.auditorId = bg.auditorId;
    fg.auditTime = bg.auditTime;
    fg.createTime = bg.createTime;
    fg.updateTime = bg.updateTime;

    // 8. 用户信息映射
    if (bg.sellerId) {
        fg.seller = {
            id: bg.sellerId,
            name: bg.sellerName || '未知用户',
            avatar: bg.sellerAvatar || ''
        };
    }

    // 9. 图片处理 (JSON字符串转数组)
    if (typeof bg.images === 'string') {
        try {
            // 尝试解析 JSON
            if (bg.images.startsWith('[')) {
                fg.images = JSON.parse(bg.images);
            } else if (bg.images.includes(',')) {
                fg.images = bg.images.split(',');
            } else {
                fg.images = [bg.images];
            }
        } catch (e) {
            fg.images = [bg.images];
        }
    } else if (Array.isArray(bg.images)) {
        fg.images = bg.images;
    } else {
        fg.images = [];
    }

    return fg;
  };

  /**
   * 计算统计数据
   */
  const getStats = () => {
    return GoodsStats.calculate(vm.goodsList);
  };

  /**
   * 获取筛选后的商品列表
   */
  const getFilteredGoods = (filterStatus, searchQuery, sortBy) => {
    let filtered = GoodsFilter.filter(vm.goodsList, filterStatus, searchQuery);
    return GoodsSorter.sort(filtered, sortBy);
  };

  /**
   * 预览商品
   */
  const handlePreview = (good) => {
    MessageHelper.info(vm, `预览商品详情页: ${good.name}`);
  };

  /**
   * 编辑商品
   */
  const handleEdit = (good, callback) => {
    const form = { ...good };

    // 映射后端字段到前端表单字段
    form.name = good.name || good.good_name;

    // 保留 categoryId（CategorySelector 直接绑定 categoryId）
    if (good.categoryId) {
      form.categoryId = good.categoryId;
    } else if (good.category) {
      // 兼容：如果只有分类名称，通过映射表查找 ID
      form.categoryId = CATEGORY_MAP[good.category] || null;
    }

    // 映射成色等级到前端 condition
    if (good.conditionLevel) {
      if (good.conditionLevel >= 10) form.condition = 'brand_new';
      else if (good.conditionLevel >= 9) form.condition = 'like_new';
      else if (good.conditionLevel >= 8) form.condition = 'good';
      else form.condition = 'fair';
    }

    // 确保 images 是数组
    if (form.images && !Array.isArray(form.images)) {
        try {
            form.images = JSON.parse(form.images);
        } catch (e) {
            // 如果不是 JSON，尝试逗号分隔
            if (typeof form.images === 'string' && form.images.includes(',')) {
              form.images = form.images.split(',');
            } else {
              form.images = [form.images];
            }
        }
    } else if (!form.images) {
        form.images = [];
    }
    // 兼容旧的 image 字段
    if (good.image && (!form.images || form.images.length === 0)) {
        form.images = Array.isArray(good.image) ? good.image : [good.image];
    }

    if (callback) callback(form);
  };

  /**
   * 删除商品
   */
  const handleDelete = async (good) => {
    try {
      await ConfirmHelper.confirmDelete(vm, good.name);
      
      const res = await vm.$axios.delete(`/api/goods/${good.id}`);
      if (isSuccess(res)) {
        MessageHelper.success(vm, '删除成功');
        await vm.fetchGoods();
        return true;
      } else {
        MessageHelper.error(vm, res.message || '删除失败');
        return false;
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
      }
      return false;
    }
  };

  /**
   * 审核通过
   */
  const handleApprove = async (good) => {
    try {
      await ConfirmHelper.confirmApprove(vm);
      
      const res = await vm.$axios.post(`/api/goods/${good.id}/approve`);
      if (isSuccess(res)) {
        MessageHelper.success(vm, '审核通过，商品已自动上架');
        await vm.fetchGoods();
        
        // 尝试从新列表中找到该商品以显示交换码
        const updatedGood = vm.goodsList.find(g => g.id === good.id);
        if (updatedGood && updatedGood.exchangeCode) {
          vm.currentExchangeCode = updatedGood.exchangeCode;
          vm.codeDialogVisible = true;
        }
        return true;
      } else {
        MessageHelper.error(vm, res.message || '操作失败');
        return false;
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error);
        MessageHelper.error(vm, '操作失败');
      }
      return false;
    }
  };

  /**
   * 驳回商品
   */
  const handleReject = async (good, reason) => {
    if (!reason) {
      MessageHelper.warning(vm, '请输入驳回理由');
      return false;
    }
    
    try {
      const res = await vm.$axios.post(`/api/goods/${good.id}/reject`, { reason });
      if (isSuccess(res)) {
        MessageHelper.warning(vm, '已驳回该商品');
        await vm.fetchGoods();
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
  };

  /**
   * 切换上下架状态
   */
  const handleToggleShelf = async (good) => {
    // 此时 good.isActive 已经被 v-model 修改了
    // 如果 isActive 为 true，说明用户想上架 (shelfStatus -> 1)
    // 如果 isActive 为 false，说明用户想下架 (shelfStatus -> 0)
    const newShelfStatus = good.isActive ? 1 : 0;
    const originalIsActive = !good.isActive; // 用于回滚

    try {
      // 构造完整的更新对象，避免后端因部分更新导致数据丢失或校验失败
      const payload = {
        id: good.id,
        name: good.name,
        description: good.description,
        // 使用 categoryId
        categoryId: good.categoryId || CATEGORY_MAP[good.category] || 7,
        // 映射成色等级
        conditionLevel: CONDITION_LEVEL_MAP[good.condition] || 3,
        deliveryType: good.deliveryType || 'pickup',
        images: Array.isArray(good.images) ? JSON.stringify(good.images) : good.images,
        // 关键：设置新的上下架状态
        shelfStatus: newShelfStatus,
        status: good.status === GOOD_STATUS.PENDING ? GOOD_STATUS.ACTIVE : undefined
      };

      // 补充映射逻辑
      if (good.condition === 'brand_new') payload.conditionLevel = 10;
      else if (good.condition === 'like_new') payload.conditionLevel = 9;
      else if (good.condition === 'good') payload.conditionLevel = 8;
      else if (good.condition === 'fair') payload.conditionLevel = 7;

      const res = await vm.$axios.put(`/api/goods/${good.id}`, payload);

      if (isSuccess(res)) {
        if (newShelfStatus === 1) {
          MessageHelper.success(vm, '商品已上架');
        } else {
          MessageHelper.info(vm, '商品已下架');
        }
        await vm.fetchGoods();
      } else {
        // 恢复状态
        good.isActive = originalIsActive;
        MessageHelper.error(vm, res.message || '操作失败');
      }
    } catch (error) {
      // 恢复状态
      good.isActive = originalIsActive;
      console.error(error);
      MessageHelper.error(vm, '操作失败');
    }
  };

  /**
   * 保存商品
   */
  const handleSave = async (form) => {
    try {
      // 1. 基础字段非空校验
      if (!form.name || !form.name.trim()) {
        MessageHelper.warning(vm, '请输入商品名称');
        return false;
      }
      if (!form.categoryId) {
        MessageHelper.warning(vm, '请选择商品分类');
        return false;
      }
      if (!form.condition) {
        MessageHelper.warning(vm, '请选择新旧程度');
        return false;
      }
      if (!form.region || !form.region.trim()) {
        MessageHelper.warning(vm, '请输入所在地区');
        return false;
      }
      if (!form.school || !form.school.trim()) {
        MessageHelper.warning(vm, '请输入所在学校');
        return false;
      }
      if (!form.deliveryType) {
        MessageHelper.warning(vm, '请选择交付方式');
        return false;
      }
      if (!form.description || !form.description.trim()) {
        MessageHelper.warning(vm, '请输入商品描述');
        return false;
      }

      // 2. 校验图片数量 (1-3张)
      const imageCount = Array.isArray(form.images) ? form.images.length : 0;
      if (imageCount === 0) {
        MessageHelper.warning(vm, '请至少上传一张商品图片');
        return false;
      }
      if (imageCount > 3) {
        MessageHelper.warning(vm, '最多只能上传三张商品图片');
        return false;
      }

      // 准备后端接口所需的参数
      const payload = {
        name: form.name,
        description: form.description,
        categoryId: form.categoryId, // 直接使用 categoryId
        conditionLevel: CONDITION_LEVEL_MAP[form.condition] || 3,
        deliveryType: form.deliveryType || 'pickup',
        images: Array.isArray(form.images) ? JSON.stringify(form.images) : form.images
      };

      // 映射成色等级到 1-10
      if (form.condition === 'brand_new') payload.conditionLevel = 10;
      else if (form.condition === 'like_new') payload.conditionLevel = 9;
      else if (form.condition === 'good') payload.conditionLevel = 8;
      else if (form.condition === 'fair') payload.conditionLevel = 7;

      let res;
      if (form.id) {
        // 更新商品
        res = await vm.$axios.put(`/api/goods/${form.id}`, payload);
        if (isSuccess(res)) {
          MessageHelper.success(vm, '更新成功');
        } else {
          MessageHelper.error(vm, res.message || '更新失败');
          return false;
        }
      } else {
        // 创建商品
        res = await vm.$axios.post('/api/goods', payload);
        if (isSuccess(res)) {
          MessageHelper.success(vm, '创建成功');
        } else {
          MessageHelper.error(vm, res.message || '创建失败');
          return false;
        }
      }
      await vm.fetchGoods();
      return true;
    } catch (error) {
      console.error('保存商品失败:', error);
      MessageHelper.error(vm, '保存失败');
      return false;
    }
  };

  /**
   * 清除筛选条件
   */
  const clearFilters = (callback) => {
    if (callback) {
      callback({
        searchQuery: '',
        filterStatus: GOOD_STATUS.ALL,
        sortBy: SORT_OPTIONS.DEFAULT
      });
    }
  };

  return {
    // 计算方法
    getStats,
    getFilteredGoods,
    
    // 操作方法
    handlePreview,
    handleEdit,
    handleDelete,
    handleApprove,
    handleReject,
    handleToggleShelf,
    handleSave,
    clearFilters,
    normalizeGood
  };
}

/**
 * 商品筛选状态管理 Composable
 */
export function useGoodsFilter() {
  return {
    defaultStatus: GOOD_STATUS.ALL,
    defaultSort: SORT_OPTIONS.DEFAULT,
    defaultSearch: ''
  };
}

/**
 * 商品对话框管理 Composable
 */
export function useGoodsDialogs() {
  return {
    // 初始表单数据
    getInitialForm: () => ({
      id: null,
      name: '',
      description: '',
      categoryId: null,
      condition: 'like_new',
      region: '',
      school: '',
      images: [],
      deliveryType: 'pickup'
    })
  };
}
