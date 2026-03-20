const KEY = 'rememberMe'

export function getRememberMe() {
  try {
    const data = localStorage.getItem(KEY)
    return data ? JSON.parse(data) : null
  } catch (e) {
    return null
  }
}

export function setRememberMe(data) {
  try {
    localStorage.setItem(KEY, JSON.stringify(data))
  } catch (e) {}
}

export function clearRememberMe() {
  localStorage.removeItem(KEY)
}
