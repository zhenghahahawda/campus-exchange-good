// 主题配置文件 - 校园换物平台
export const themeCategories = [
  { id: 'all', name: '全部', icon: '🎨', count: 0 },
  { id: 'dark', name: '深色', icon: '🌙', count: 0 },
  { id: 'light', name: '浅色', icon: '☀️', count: 0 },
  { id: 'tech', name: '科技', icon: '🚀', count: 0 },
  { id: 'nature', name: '自然', icon: '🌿', count: 0 },
  { id: 'luxury', name: '奢华', icon: '💎', count: 0 }
];

export const themes = [
  {
    id: 'default',
    name: '深蓝色主题',
    description: '经典深色科技风格',
    gradient: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
    primaryColor: '#0075ff',
    sidebarBg: 'rgba(30, 41, 59, 0.6)',
    tags: ['深色', '科技', '经典'],
    category: ['dark', 'tech']
  },
  {
    id: 'dark-gold',
    name: '暗黑金主题',
    description: '奢华高端黑金配色',
    gradient: 'linear-gradient(135deg, #0A0A0A 0%, #1A1A1A 100%)',
    primaryColor: '#D4AF37',
    sidebarBg: 'rgba(26, 26, 26, 0.7)',
    tags: ['深色', '奢华', '高端'],
    category: ['dark', 'luxury']
  },
  {
    id: 'cyberpunk',
    name: '赛博朋克主题',
    description: '未来科幻霓虹灯风格',
    gradient: 'linear-gradient(135deg, #0D0221 0%, #1A0B2E 100%)',
    primaryColor: '#FF00FF',
    sidebarBg: 'rgba(26, 11, 46, 0.6)',
    tags: ['深色', '科幻', '霓虹'],
    category: ['dark', 'tech']
  },
  {
    id: 'neon-night',
    name: '霓虹夜市主题',
    description: '亚洲街头夜市风格',
    gradient: 'linear-gradient(135deg, #08090A 0%, #1A1D23 100%)',
    primaryColor: '#FF2E63',
    sidebarBg: 'rgba(26, 29, 35, 0.7)',
    tags: ['深色', '活力', '街头'],
    category: ['dark', 'tech']
  },
  {
    id: 'deep-ocean',
    name: '深海探索主题',
    description: '神秘宁静深海风格',
    gradient: 'linear-gradient(135deg, #001F3F 0%, #003D5C 100%)',
    primaryColor: '#00CED1',
    sidebarBg: 'rgba(0, 61, 92, 0.6)',
    tags: ['深色', '神秘', '宁静'],
    category: ['dark', 'nature']
  },
  {
    id: 'aurora',
    name: '北极光主题',
    description: '梦幻极光流动风格',
    gradient: 'linear-gradient(135deg, #0A1128 0%, #1C2541 100%)',
    primaryColor: '#00FFA3',
    sidebarBg: 'rgba(28, 37, 65, 0.6)',
    tags: ['深色', '梦幻', '极光'],
    category: ['dark', 'nature']
  },
  {
    id: 'iced-americano',
    name: '冰美式主题',
    description: '沉稳提神咖啡色调',
    gradient: 'linear-gradient(135deg, #1A1512 0%, #2C241F 100%)',
    primaryColor: '#C69F7F',
    sidebarBg: 'rgba(44, 36, 31, 0.6)',
    tags: ['深色', '沉稳', '咖啡'],
    category: ['dark', 'luxury']
  },
  {
    id: 'purple',
    name: '浅紫色主题',
    description: '清新明亮办公风格',
    gradient: 'linear-gradient(135deg, #F7F8FC 0%, #E4D7FF 100%)',
    primaryColor: '#6C5DD3',
    sidebarBg: 'rgba(255, 255, 255, 0.45)',
    tags: ['浅色', '清新', '办公'],
    category: ['light']
  },
  {
    id: 'sakura',
    name: '樱花季主题',
    description: '浪漫温柔日系风格',
    gradient: 'linear-gradient(135deg, #FFF5F7 0%, #FFE4E9 100%)',
    primaryColor: '#FF69B4',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '浪漫', '日系'],
    category: ['light', 'nature']
  },
  {
    id: 'mint-ice',
    name: '薄荷冰沙主题',
    description: '清凉舒适夏日风格',
    gradient: 'linear-gradient(135deg, #F0FFFF 0%, #E0F2F1 100%)',
    primaryColor: '#00D9C0',
    sidebarBg: 'rgba(255, 255, 255, 0.7)',
    tags: ['浅色', '清凉', '夏日'],
    category: ['light', 'nature']
  },
  {
    id: 'yellow',
    name: '暖黄色主题',
    description: '温暖活力琥珀色调',
    gradient: 'linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%)',
    primaryColor: '#F59E0B',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '温暖', '活力'],
    category: ['light', 'nature']
  },
  {
    id: 'green',
    name: '清新绿色主题',
    description: '自然环保健康风格',
    gradient: 'linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%)',
    primaryColor: '#059669',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '自然', '健康'],
    category: ['light', 'nature']
  },
  {
    id: 'red',
    name: '热情红色主题',
    description: '醒目热情玫瑰红调',
    gradient: 'linear-gradient(135deg, #FFF1F2 0%, #FFE4E6 100%)',
    primaryColor: '#E11D48',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '热情', '醒目'],
    category: ['light']
  },
  {
    id: 'orange',
    name: '活力橙色主题',
    description: '创意活力鲜橙风格',
    gradient: 'linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%)',
    primaryColor: '#F97316',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '活力', '创意'],
    category: ['light']
  },
  {
    id: 'lightblue',
    name: '清爽淡蓝主题',
    description: '宁静天空清爽色调',
    gradient: 'linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%)',
    primaryColor: '#0EA5E9',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '清爽', '宁静'],
    category: ['light', 'nature']
  },
  {
    id: 'white',
    name: '极简亮白主题',
    description: '纯净现代极简风格',
    gradient: 'linear-gradient(135deg, #F9FAFB 0%, #FFFFFF 100%)',
    primaryColor: '#171717',
    sidebarBg: 'rgba(255, 255, 255, 0.8)',
    tags: ['浅色', '极简', '现代'],
    category: ['light']
  },
  {
    id: 'rainbow',
    name: '梦幻彩虹主题',
    description: '多彩梦幻独角兽风格',
    gradient: 'linear-gradient(135deg, #FAF5FF 0%, #F3E8FF 100%)',
    primaryColor: '#D946EF',
    sidebarBg: 'rgba(255, 255, 255, 0.75)',
    tags: ['浅色', '梦幻', '多彩'],
    category: ['light']
  },
  {
    id: 'galaxy',
    name: '星际迷航主题',
    description: '深邃神秘星空风格',
    gradient: 'linear-gradient(135deg, #0F172A 0%, #312E81 100%)',
    primaryColor: '#818CF8',
    sidebarBg: 'rgba(49, 46, 129, 0.6)',
    tags: ['深色', '神秘', '星空'],
    category: ['dark', 'tech', 'nature']
  },
  {
    id: 'peach-fuzz',
    name: '柔和桃绒主题',
    description: '温柔治愈年度流行色',
    gradient: 'linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%)',
    primaryColor: '#FFBE98',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '柔和', '温暖'],
    category: ['light', 'nature']
  },
  {
    id: 'teal-waters',
    name: '碧水蓝天主题',
    description: '清新自然海洋风格',
    gradient: 'linear-gradient(135deg, #042F2E 0%, #115E59 100%)',
    primaryColor: '#2DD4BF',
    sidebarBg: 'rgba(17, 94, 89, 0.6)',
    tags: ['深色', '清新', '海洋'],
    category: ['dark', 'nature']
  },
  {
    id: 'sunset-drive',
    name: '日落大道主题',
    description: '复古未来落日风格',
    gradient: 'linear-gradient(135deg, #4C1D95 0%, #BE185D 100%)',
    primaryColor: '#F472B6',
    sidebarBg: 'rgba(76, 29, 149, 0.6)',
    tags: ['深色', '复古', '梦幻'],
    category: ['dark', 'tech']
  },
  {
    id: 'matcha-zen',
    name: '抹茶禅意主题',
    description: '淡雅宁静抹茶风格',
    gradient: 'linear-gradient(135deg, #ECFCCB 0%, #D9F99D 100%)',
    primaryColor: '#65A30D',
    sidebarBg: 'rgba(255, 255, 255, 0.6)',
    tags: ['浅色', '自然', '清新'],
    category: ['light', 'nature']
  },
  {
    id: 'midnight-pro',
    name: '午夜极客主题',
    description: '极致深邃极客风格',
    gradient: 'linear-gradient(135deg, #020617 0%, #0F172A 100%)',
    primaryColor: '#38BDF8',
    sidebarBg: 'rgba(15, 23, 42, 0.8)',
    tags: ['深色', '极简', '专业'],
    category: ['dark', 'tech']
  },
  {
    id: 'rose-gold',
    name: '玫瑰金主题',
    description: '奢华优雅玫瑰金风格',
    gradient: 'linear-gradient(135deg, #FFF0F3 0%, #FFF5F7 100%)',
    primaryColor: '#E11D48',
    sidebarBg: 'rgba(255, 240, 243, 0.6)',
    tags: ['浅色', '奢华', '优雅'],
    category: ['light', 'luxury']
  },
  {
    id: 'abyssal-blue',
    name: '深渊蓝主题',
    description: '极致深沉静谧深渊',
    gradient: 'linear-gradient(135deg, #000428 0%, #004e92 100%)',
    primaryColor: '#00D2FF',
    sidebarBg: 'rgba(0, 4, 40, 0.8)',
    tags: ['深色', '神秘', '深邃'],
    category: ['dark', 'tech']
  },
  {
    id: 'vintage-paper',
    name: '复古羊皮纸主题',
    description: '怀旧温暖纸质风格',
    gradient: 'linear-gradient(135deg, #FEF9E7 0%, #F9E79F 100%)',
    primaryColor: '#8D6E63',
    sidebarBg: 'rgba(254, 249, 231, 0.6)',
    tags: ['浅色', '复古', '怀旧'],
    category: ['light', 'nature']
  }
];