/**
 * 自动生成
 *
 * @author 徐晓伟 <xuxiaowei@xuxiaowei.com.cn>
 */

/**
 * 数字
 */
export const NUMBER = '0123456789'

/**
 * 小写字母
 */
export const LOWER_CASE = 'abcdefghijklmnopqrstuvwxyz'

/**
 * 大写字母
 */
export const UPPER_CASE = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

/**
 * 符号
 */
export const SYMBOL = '!@#$%^&*()-_=+[{]};:,<.>/?'

/**
 * 生成密码选项
 */
class PasswordOption {
  /**
   * 数字个数
   */
  number: number

  /**
   * 小写字母个数
   */
  lowerCase: number

  /**
   * 大写字母个数
   */
  upperCase: number

  /**
   * 符号个数
   */
  symbol: number

  constructor () {
    this.number = 3
    this.lowerCase = 1
    this.upperCase = 1
    this.symbol = 1
  }
}

/**
 * 字符串生成
 * @param chars
 * @param length
 */
const generate = (chars: string[], length: number) => {
  let password = ''
  for (let i = 0; i < length; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return password
}

/**
 * 生成密码
 * @param option
 */
export const generatePassword = (option?: PasswordOption = new PasswordOption()): string => {
  let password = ''

  password += generate(NUMBER, option.number)
  password += generate(LOWER_CASE, option.lowerCase)
  password += generate(UPPER_CASE, option.upperCase)
  password += generate(SYMBOL, option.symbol)

  return password.split('').sort(function () {
    return Math.random() - 0.3
  }).join('')
}