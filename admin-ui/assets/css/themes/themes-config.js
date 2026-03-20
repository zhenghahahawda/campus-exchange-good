// 主题配置文件
export const themeCategories = [
  { id: 'all', name: '全部', icon: '🎨', count: 0 },
  { id: 'dark', name: '深色', icon: '🌙', count: 0 },
  { id: 'light', name: '浅色', icon: '☀️', count: 0 },
  { id: 'tech', name: '科技', icon: '🚀', count: 0 },
  { id: 'nature', name: '自然', icon: '🌿', count: 0 },
  { id: 'luxury', name: '奢华', icon: '💎', count: 0 },
  { id: 'static', name: '静态', icon: '🖼️', count: 0 },
  { id: 'video', name: '动态', icon: '🎬', count: 0 }
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
  },
  {
    id: 'ios-style',
    name: 'iOS 浅色风格',
    description: '苹果系统经典扁平化与毛玻璃风格',
    gradient: 'linear-gradient(135deg, #F2F2F7 0%, #FFFFFF 100%)',
    primaryColor: '#007AFF',
    sidebarBg: 'rgba(255, 255, 255, 0.7)',
    tags: ['浅色', '苹果', '极简'],
    category: ['light', 'tech', 'luxury']
  },
  {
    id: 'ios-dark',
    name: 'iOS 暗黑风格',
    description: '苹果系统深色模式风格',
    gradient: 'linear-gradient(135deg, #000000 0%, #1C1C1E 100%)',
    primaryColor: '#0A84FF',
    sidebarBg: 'rgba(28, 28, 30, 0.7)',
    tags: ['深色', '苹果', '极简'],
    category: ['dark', 'tech', 'luxury']
  },
  {
    id: 'synthwave',
    name: '合成器浪潮',
    description: '80年代复古未来主义，紫橙渐变，带你重返迈阿密之夜',
    gradient: 'linear-gradient(135deg, #2b0f4c 0%, #ff5e00 100%)',
    primaryColor: '#00f0ff',
    sidebarBg: 'rgba(43, 15, 76, 0.8)',
    tags: ['深色', '复古', '霓虹', '创意'],
    category: ['dark', 'tech']
  },
  {
    id: 'glass-morphism-pro',
    name: '全息玻璃态',
    description: '极高透明度与流光溢彩的炫彩背景，突破次元壁的视觉体验',
    gradient: 'linear-gradient(120deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)',
    primaryColor: '#a18cd1',
    sidebarBg: 'rgba(255, 255, 255, 0.2)',
    tags: ['浅色', '玻璃拟态', '炫彩', '未来'],
    category: ['light', 'luxury']
  },
  {
    id: 'anime-lineart',
    name: 'Kuroha ',
    description: '动态黑白线稿风格，极致的线条艺术',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/anime-lineart.mp4',
    gradient: 'linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%)', // Fallback
    primaryColor: '#333333',
    sidebarBg: 'rgba(255, 255, 255, 0.8)',
    tags: ['浅色', '动态', '视频', '线稿'],
    category: ['light', 'creative', 'video']
  },
  {
    id: 'ink-samurai',
    name: '水墨武士',
    description: '传统水墨与动态武侠的完美结合，展现东方美学意境',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/ink-samurai.mp4',
    gradient: 'linear-gradient(135deg, #e6e6e6 0%, #ffffff 100%)', // Fallback
    primaryColor: '#2c3e50',
    sidebarBg: 'rgba(240, 240, 240, 0.7)',
    tags: ['浅色', '动态', '视频', '国风', '水墨'],
    category: ['light', 'creative', 'video', 'culture']
  },
  {
    id: 'anime-girl',
    name: '果园偶遇',
    description: '梦幻唯美的二次元少女动态壁纸',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/anime-girl.mp4',
    gradient: 'linear-gradient(135deg, #FEF9C3 0%, #FDE047 100%)', // Fallback
    primaryColor: '#EAB308',
    sidebarBg: 'rgba(255, 255, 255, 0.8)',
    tags: ['浅色', '动态', '视频', '二次元', '少女'],
    category: ['light', 'creative', 'video']
  },
  {
    id: 'starry-night',
    name: '星空之夜',
    description: 'AI 绘制的梦幻夜晚星空',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/starry-night.mp4',
    gradient: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)', // Fallback
    primaryColor: '#6366f1',
    sidebarBg: 'rgba(15, 23, 42, 0.6)',
    tags: ['深色', '动态', '视频', '星空', 'AI'],
    category: ['dark', 'creative', 'video']
  },
  {
    id: 'anime-glow',
    name: '幻光魅影',
    description: '绚丽的二次元光效动态壁纸',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/anime-glow.mp4',
    gradient: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%)', // Fallback
    primaryColor: '#3b82f6',
    sidebarBg: 'rgba(15, 23, 42, 0.7)',
    tags: ['深色', '动态', '视频', '光效', '炫酷'],
    category: ['dark', 'creative', 'video']
  },
  {
    id: 'mountain-coast',
    name: '山林海岸',
    description: '宁静的山林与壮丽的海岸线动态壁纸',
    // 视频壁纸特殊标记
    video: '/images/wallpapers/mountain-coast.mp4',
    gradient: 'linear-gradient(135deg, #0f172a 0%, #0d9488 100%)', // Fallback
    primaryColor: '#0d9488',
    sidebarBg: 'rgba(15, 23, 42, 0.6)',
    tags: ['深色', '动态', '视频', '山林', '海岸'],
    category: ['dark', 'nature', 'video']
  },
  {
    id: 'windows-red',
    name: '我的window黑化了',
    description: '深邃的暗红流动纹理，经典 Windows 风格',
    wallpaper: '/images/wallpapers/windows-red-abstract.png',
    gradient: 'linear-gradient(135deg, #0f172a 0%, #b91c1c 100%)', // Fallback
    primaryColor: '#ef4444',
    sidebarBg: 'rgba(15, 23, 42, 0.6)',
    tags: ['深色', '静态', 'Windows', '红色'],
    category: ['dark', 'tech', 'static']
  },
  {
    id: 'bliss-breach',
    name: '耄耋哈气突破次元',
    description: '破碎的 Bliss 壁纸中冲出的喷火小猫，充满混沌与趣味',
    wallpaper: '/images/wallpapers/windows-firecat.png',
    gradient: 'linear-gradient(135deg, #2EA3FF 0%, #66A53A 100%)', // Fallback
    primaryColor: '#2EA3FF',
    sidebarBg: 'rgba(255, 255, 255, 0.45)', // 浅色磨砂
    tags: ['浅色', '静态', 'Windows', '趣味', '小猫'],
    category: ['light', 'nature', 'static', 'creative']
  }
];
