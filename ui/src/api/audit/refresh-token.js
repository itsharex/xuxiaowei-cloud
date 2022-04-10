import request from '@/utils/request'

/**
 * 分页查询刷新Token
 * @param data
 * @returns {*}
 */
export const page = function (data) {
  return request.post('/audit/oauth-refresh-token/page', data).then(response => {
    return response.data
  })
}
