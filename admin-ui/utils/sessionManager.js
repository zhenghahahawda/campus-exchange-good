import { SESSION_STATUS } from './sessionConstants';

export const SessionStats = {
  generateStatCards(list) {
    const total = list.length;
    const active = list.filter(item => item.isActive === SESSION_STATUS.ACTIVE).length;
    const inactive = total - active;
    
    return [
      { title: '总会话', value: total, icon: 'el-icon-monitor', color: 'primary', status: 'all' },
      { title: '活跃会话', value: active, icon: 'el-icon-circle-check', color: 'success', status: SESSION_STATUS.ACTIVE },
      { title: '已失效', value: inactive, icon: 'el-icon-circle-close', color: 'info', status: SESSION_STATUS.INACTIVE }
    ];
  }
};
