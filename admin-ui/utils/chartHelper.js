/**
 * 图表工具类
 * 提供图表配置和主题管理功能
 */

/**
 * 获取当前主题的颜色变量
 * @returns {Object} 颜色对象
 */
export function getThemeColors() {
  const style = getComputedStyle(document.documentElement);
  return {
    textSecondary: style.getPropertyValue('--color-text-secondary').trim(),
    border: style.getPropertyValue('--color-border').trim(),
    primary: style.getPropertyValue('--color-primary').trim(),
    warning: style.getPropertyValue('--color-secondary-warning').trim()
  };
}

/**
 * 获取订单图表配置
 * @param {Object} colors - 主题颜色
 * @returns {Object} ECharts 配置对象
 */
export function getOrderChartOption(colors) {
  return {
    animation: true,
    animationDuration: 1000,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      textStyle: { color: '#333' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
      borderColor: 'transparent'
    },
    xAxis: {
      type: 'category',
      data: ['7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.textSecondary }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: colors.border } },
      axisLabel: { color: colors.textSecondary }
    },
    series: [{
      name: '订单量',
      data: [25, 20, 30, 22, 17, 29],
      type: 'bar',
      barWidth: '10px',
      itemStyle: {
        color: colors.primary,
        borderRadius: [4, 4, 0, 0]
      },
      showBackground: false
    }]
  };
}

/**
 * 获取销售图表配置
 * @param {Object} colors - 主题颜色
 * @returns {Object} ECharts 配置对象
 */
export function getSalesChartOption(colors) {
  return {
    animation: true,
    animationDuration: 1000,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      textStyle: { color: '#333' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
      borderColor: 'transparent'
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.textSecondary }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: colors.border } },
      axisLabel: { color: colors.textSecondary }
    },
    series: [{
      name: '销售额',
      data: [150, 230, 224, 218, 135, 147, 260],
      type: 'line',
      smooth: true,
      lineStyle: { color: colors.primary, width: 3 },
      itemStyle: { color: colors.primary },
      showSymbol: false,
      areaStyle: {
        color: colors.primary,
        opacity: 0.2
      }
    }]
  };
}

/**
 * 更新图表主题
 * @param {Object} chart - ECharts 实例
 */
export function updateChartTheme(chart) {
  if (!chart) return;

  const colors = getThemeColors();
  chart.setOption({
    xAxis: { axisLabel: { color: colors.textSecondary } },
    yAxis: {
      splitLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textSecondary }
    },
    series: [{
      itemStyle: { color: colors.primary },
      lineStyle: { color: colors.primary },
      areaStyle: { color: colors.primary }
    }]
  });
}
