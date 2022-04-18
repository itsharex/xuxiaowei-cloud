import request from '../utils/request'
import store from '../store'

/**
 * 检查 Token
 * @param token
 */
export const checkToken = function (token: string) {
  return request.post('/authorization-server/oauth/check_token?token=' + token).then(response => {
    const responseData = response.data
    if (responseData && responseData.active === true) {
      const authorities = responseData.authorities
      store.commit('setAuthorities', authorities)
    }
    return response.data
  })
}