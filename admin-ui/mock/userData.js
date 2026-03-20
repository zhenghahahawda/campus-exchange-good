import { USER_STATUS, USER_ROLES } from '@/utils/userConstants';

/**
 * 用户模拟数据
 */
export const mockUsers = [
  {
    id: 1,
    name: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138001',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: USER_ROLES.ADMIN,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 15,
    ordersCount: 28,
    loginCount: 156,
    createdAt: '2024-01-15',
    userId: 'U001',
    accountAddress: '北京市海淀区',
    school: '北京大学',
    isOnline: true,
    lastLoginTime: '2024-03-06 14:30:25'
  },
  {
    id: 2,
    name: '李四',
    email: 'lisi@example.com',
    phone: '13800138002',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 8,
    ordersCount: 12,
    loginCount: 89,
    createdAt: '2024-02-20',
    userId: 'U002',
    accountAddress: '北京市朝阳区',
    school: '清华大学',
    isOnline: false,
    lastLoginTime: '2024-03-05 09:15:42'
  },
  {
    id: 3,
    name: '王五',
    email: 'wangwu@example.com',
    phone: '13800138003',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.INACTIVE,
    isActive: false,
    goodsCount: 3,
    ordersCount: 5,
    loginCount: 23,
    createdAt: '2024-03-10',
    userId: 'U003',
    accountAddress: '上海市浦东新区',
    school: '复旦大学',
    isOnline: false,
    lastLoginTime: '2024-02-28 16:45:18'
  },
  {
    id: 4,
    name: '赵六',
    email: 'zhaoliu@example.com',
    phone: '13800138004',
    avatar: 'https://cube.elemecdn.com/6/94/4d3ea53c4e4e7c3e5fc0c165f7b2dpng.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 22,
    ordersCount: 45,
    loginCount: 234,
    createdAt: '2023-12-05',
    userId: 'U004',
    accountAddress: '上海市徐汇区',
    school: '上海交通大学',
    isOnline: true,
    lastLoginTime: '2024-03-06 13:22:10'
  },
  {
    id: 5,
    name: '孙七',
    email: 'sunqi@example.com',
    phone: '13800138005',
    avatar: 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 5,
    ordersCount: 8,
    loginCount: 45,
    createdAt: '2024-01-28',
    userId: 'U005',
    accountAddress: '浙江省杭州市',
    school: '浙江大学',
    isOnline: false,
    lastLoginTime: '2024-03-04 20:33:55'
  },
  {
    id: 6,
    name: '钱八',
    email: 'qianba@example.com',
    phone: '13800138006',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 12,
    ordersCount: 25,
    loginCount: 78,
    createdAt: '2024-01-10',
    userId: 'U006',
    accountAddress: '江苏省南京市',
    school: '南京大学',
    isOnline: true,
    lastLoginTime: '2024-03-06 11:45:30'
  },
  {
    id: 7,
    name: '周九',
    email: 'zhoujiu@example.com',
    phone: '13800138007',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    role: USER_ROLES.USER,
    status: USER_STATUS.ACTIVE,
    isActive: true,
    goodsCount: 3,
    ordersCount: 6,
    loginCount: 32,
    createdAt: '2024-02-15',
    userId: 'U007',
    accountAddress: '广东省广州市',
    school: '中山大学',
    isOnline: false,
    lastLoginTime: '2024-03-03 15:20:12'
  }
];

/**
 * 获取模拟用户数据
 * @returns {Array} 用户列表
 */
export function getMockUsers() {
  return [...mockUsers];
}