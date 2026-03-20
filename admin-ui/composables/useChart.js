/**
 * 图表管理 Composable
 * 处理图表初始化、主题切换和资源清理
 */
import * as echarts from 'echarts';
import { updateChartTheme } from '@/utils/chartHelper';

export function useChart() {
  let chart = null;
  let resizeHandler = null;
  let themeObserver = null;

  /**
   * 初始化图表
   * @param {HTMLElement} container - 图表容器元素
   * @param {Object} option - 图表配置
   * @param {Function} [onThemeChange] - 主题变化时的回调，不传则使用默认更新
   */
  const initChart = (container, option, onThemeChange) => {
    if (!container) return;

    chart = echarts.init(container, null, { renderer: 'canvas' });
    chart.setOption(option);

    // 防抖的窗口大小调整处理
    let timeout;
    resizeHandler = () => {
      clearTimeout(timeout);
      timeout = setTimeout(() => chart?.resize(), 100);
    };
    window.addEventListener('resize', resizeHandler);

    // 监听主题变化
    themeObserver = new MutationObserver(() => {
      if (onThemeChange) {
        onThemeChange()
      } else {
        updateChartTheme(chart)
      }
    });
    themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme']
    });
  };

  /**
   * 清理资源
   */
  const dispose = () => {
    themeObserver?.disconnect();
    if (resizeHandler) window.removeEventListener('resize', resizeHandler);
    chart?.dispose();
    chart = null;
  };

  /**
   * 更新图表配置
   * @param {Object} option - 新的图表配置
   */
  const setOption = (option) => {
    chart?.setOption(option, true);
  };

  return {
    initChart,
    setOption,
    dispose
  };
}
