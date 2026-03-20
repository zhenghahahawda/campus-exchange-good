export const LOGIN_STATUS = {
  SUCCESS: 1,
  FAILED: 0,
  ALL: -1
};

export const LOGIN_STATUS_LABELS = {
  [LOGIN_STATUS.SUCCESS]: '成功',
  [LOGIN_STATUS.FAILED]: '失败'
};

export const LOGIN_STATUS_COLORS = {
  [LOGIN_STATUS.SUCCESS]: 'success',
  [LOGIN_STATUS.FAILED]: 'danger'
};

export const LOGIN_LOG_SORT_OPTIONS = {
  LATEST: 'latest',
  OLDEST: 'oldest'
};

export const LOGIN_LOG_SORT_LABELS = {
  [LOGIN_LOG_SORT_OPTIONS.LATEST]: '最新登录',
  [LOGIN_LOG_SORT_OPTIONS.OLDEST]: '最早登录'
};

export const PAGINATION_CONFIG = {
  DEFAULT_PAGE: 1,
  PAGE_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100]
};
