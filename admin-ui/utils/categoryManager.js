import { CATEGORY_STATUS } from './categoryConstants';

export const CategoryStats = {
  generateStatCards(list) {
    const total = list.length;
    const active = list.filter(item => item.isActive === CATEGORY_STATUS.ENABLED).length;
    const disabled = total - active;

    return [
      {
        title: '总分类',
        value: total,
        icon: 'el-icon-collection-tag',
        iconBg: 'bg-primary',
        percentage: '',
        footerLabel: '全部分类',
        status: 'all'
      },
      {
        title: '启用中',
        value: active,
        icon: 'el-icon-check',
        iconBg: 'bg-success',
        percentage: '',
        footerLabel: '正常使用',
        status: CATEGORY_STATUS.ENABLED
      },
      {
        title: '已禁用',
        value: disabled,
        icon: 'el-icon-close',
        iconBg: 'bg-info',
        percentage: '',
        footerLabel: '已停用',
        status: CATEGORY_STATUS.DISABLED
      }
    ];
  }
};
