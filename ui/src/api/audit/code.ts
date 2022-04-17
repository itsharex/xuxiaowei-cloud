import request from '../../utils/request'

/**
 * 分页查询授权码
 * @param data
 */
export const page = function (data: any) {
  return request.post('/audit/oauth-code/page', data).then(response => {
    return response.data
  })
}

/**
 * 根据 授权码Code主键 删除
 * @param codeId 授权码Code主键
 */
export const removeById = function (codeId: number) {
  return request.delete('/audit/oauth-code/removeById/' + codeId).then(response => {
    return response.data
  })
}
