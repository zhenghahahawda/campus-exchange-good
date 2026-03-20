/**
 * 通用筛选工具基类
 */

export function validateArray(items) {
  return (items && Array.isArray(items)) ? items : [];
}

export class BaseFilter {
  static filterByStatus(items, status, allStatusValue, statusField = 'status') {
    items = validateArray(items);
    if (status === allStatusValue) return items;
    return items.filter(item => item[statusField] === status);
  }

  static filterBySearch(items, query, searchFields) {
    items = validateArray(items);
    if (!query || query.trim() === '') return items;
    const lowerQuery = query.toLowerCase().trim();
    return items.filter(item =>
      searchFields.some(fieldPath => {
        const value = getNestedValue(item, fieldPath);
        return value && String(value).toLowerCase().includes(lowerQuery);
      })
    );
  }

  static filter(items, status, query) {
    items = validateArray(items);
    let filtered = this.filterByStatus(items, status);
    filtered = this.filterBySearch(filtered, query);
    return filtered;
  }
}

export class BaseSorter {
  static sort(items, sortBy, sortConfig) {
    items = validateArray(items);
    if (!sortConfig || !sortConfig[sortBy]) return [...items];

    const { field, order } = sortConfig[sortBy];
    return [...items].sort((a, b) => {
      let aValue = getNestedValue(a, field);
      let bValue = getNestedValue(b, field);

      if (typeof aValue === 'string' && !isNaN(Date.parse(aValue))) {
        aValue = new Date(aValue).getTime();
        bValue = bValue ? new Date(bValue).getTime() : 0;
      } else {
        aValue = aValue || 0;
        bValue = bValue || 0;
      }

      return order === 'asc' ? aValue - bValue : bValue - aValue;
    });
  }
}

function getNestedValue(obj, path) {
  return path.split('.').reduce((current, key) => current?.[key], obj);
}
