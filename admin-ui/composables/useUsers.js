// 注意：此文件为 Vue 3 Composition API 示例
// 在 Vue 2 项目中，相关逻辑已直接集成到组件的 Options API 中
// 此文件保留作为未来 Vue 3 迁移的参考

import { ref, computed, watch } from 'vue';
import { 
  UserFilter, 
  UserSorter, 
  UserStats, 
  UserOperations, 
  UserValidator 
} from '@/utils/userManager';
import { 
  USER_STATUS, 
  USER_SORT_OPTIONS, 
  USER_SORT_LABELS,
  PAGINATION_CONFIG,
  DEFAULT_AVATAR,
  USER_TYPES
} from '@/utils/userConstants';

/**
 * 用户管理组合式函数 (Vue 3)
 * 注意：当前项目使用 Vue 2，此函数仅作为参考
 * @param {Array} initialUsers - 初始用户列表
 * @returns {Object} 用户管理相关的响应式数据和方法
 */
export function useUsers(initialUsers = []) {
  // 响应式数据
  const usersList = ref([...initialUsers]);
  const searchQuery = ref('');
  const filterStatus = ref(USER_STATUS.ALL);
  const sortBy = ref(USER_SORT_OPTIONS.DEFAULT);
  const currentPage = ref(PAGINATION_CONFIG.DEFAULT_PAGE);
  const pageSize = ref(PAGINATION_CONFIG.PAGE_SIZE);

  // 计算属性
  const statCardsData = computed(() => {
    return UserStats.generateStatCards(usersList.value);
  });

  const filteredUsers = computed(() => {
    return UserFilter.filter(usersList.value, filterStatus.value, searchQuery.value);
  });

  const sortedUsers = computed(() => {
    return UserSorter.sort(filteredUsers.value, sortBy.value);
  });

  const paginatedUsers = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return sortedUsers.value.slice(start, end);
  });

  // 监听器 - 筛选条件变化时重置页码
  watch([searchQuery, filterStatus, sortBy], () => {
    currentPage.value = PAGINATION_CONFIG.DEFAULT_PAGE;
  });

  // 方法
  const getSortLabel = (sortOption) => {
    return USER_SORT_LABELS[sortOption] || USER_SORT_LABELS[USER_SORT_OPTIONS.DEFAULT];
  };

  const handleSortCommand = (command) => {
    sortBy.value = command;
  };

  const handlePageChange = (page) => {
    currentPage.value = page;
    // 滚动到页面顶部
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleClearFilters = () => {
    searchQuery.value = '';
    filterStatus.value = USER_STATUS.ALL;
    sortBy.value = USER_SORT_OPTIONS.DEFAULT;
    currentPage.value = PAGINATION_CONFIG.DEFAULT_PAGE;
  };

  const addUser = (formData) => {
    const newUser = UserOperations.createUser(formData, usersList.value);
    usersList.value.push(newUser);
    return newUser;
  };

  const updateUser = (updatedUserData) => {
    usersList.value = UserOperations.updateUser(usersList.value, updatedUserData);
  };

  const deleteUser = (userId) => {
    usersList.value = UserOperations.deleteUser(usersList.value, userId);
  };

  const toggleUserStatus = (user) => {
    return UserOperations.toggleUserStatus(user);
  };

  const generateUserId = () => {
    return UserOperations.generateUserId(usersList.value);
  };

  const validateForm = (formData) => {
    return UserValidator.validateUserForm(formData);
  };

  return {
    // 响应式数据
    usersList,
    searchQuery,
    filterStatus,
    sortBy,
    currentPage,
    pageSize,
    
    // 计算属性
    statCardsData,
    filteredUsers,
    sortedUsers,
    paginatedUsers,
    
    // 方法
    getSortLabel,
    handleSortCommand,
    handlePageChange,
    handleClearFilters,
    addUser,
    updateUser,
    deleteUser,
    toggleUserStatus,
    generateUserId,
    validateForm
  };
}

/**
 * 用户表单管理组合式函数 (Vue 3)
 * 注意：当前项目使用 Vue 2，此函数仅作为参考
 * @returns {Object} 表单管理相关的响应式数据和方法
 */
export function useUserForm() {
  const dialogVisible = ref(false);
  const isEditMode = ref(false);
  const currentForm = ref({
    id: null,
    name: '',
    email: '',
    phone: '',
    role: 'user',
    avatar: DEFAULT_AVATAR,
    userId: '',
    accountAddress: '',
    school: '',
    userType: USER_TYPES.NORMAL
  });

  const resetForm = () => {
    currentForm.value = {
      id: null,
      name: '',
      email: '',
      phone: '',
      role: 'user',
      avatar: DEFAULT_AVATAR,
      userId: '',
      accountAddress: '',
      school: '',
      userType: USER_TYPES.NORMAL
    };
  };

  const openAddDialog = (generateUserIdFn) => {
    isEditMode.value = false;
    resetForm();
    currentForm.value.userId = generateUserIdFn();
    dialogVisible.value = true;
  };

  const openEditDialog = (user) => {
    isEditMode.value = true;
    currentForm.value = {
      id: user.id,
      name: user.name,
      email: user.email,
      phone: user.phone || '',
      role: user.role,
      avatar: user.avatar,
      userId: user.userId,
      accountAddress: user.accountAddress,
      school: user.school,
      userType: user.loginCount > 50 ? USER_TYPES.ACTIVE : USER_TYPES.NORMAL
    };
    dialogVisible.value = true;
  };

  const closeDialog = () => {
    dialogVisible.value = false;
    resetForm();
  };

  return {
    dialogVisible,
    isEditMode,
    currentForm,
    resetForm,
    openAddDialog,
    openEditDialog,
    closeDialog
  };
}