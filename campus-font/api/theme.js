export function getActiveThemes(axios) {
  return axios({
    url: '/themes',
    method: 'get'
  })
}
