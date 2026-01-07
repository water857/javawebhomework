// 表单验证工具函数

// 手机号验证（中国手机号格式）
export function validatePhone(phone) {
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(phone);
}

// 邮箱验证
export function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// 身份证号验证（中国身份证号格式）
export function validateIdCard(idCard) {
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  return idCardRegex.test(idCard);
}

// 密码强度验证（至少8位，包含字母和数字）
export function validatePassword(password) {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
  return passwordRegex.test(password);
}

// 真实姓名验证（中文姓名，2-4个字符）
export function validateRealName(realName) {
  const realNameRegex = /^[\u4e00-\u9fa5]{2,4}$/;
  return realNameRegex.test(realName);
}

// 地址验证（长度限制）
export function validateAddress(address) {
  return address.trim().length >= 5 && address.trim().length <= 200;
}

// 表单验证错误信息
export const validationMessages = {
  phone: '请输入有效的手机号码（11位数字，以1开头）',
  email: '请输入有效的邮箱地址',
  idCard: '请输入有效的身份证号（15位或18位）',
  password: '密码至少8位，包含字母和数字',
  realName: '请输入有效的真实姓名（2-4个中文字符）',
  address: '请输入有效的地址（至少5个字符，最多200个字符）',
  confirmPassword: '两次输入的密码不一致'
};