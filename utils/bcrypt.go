package utils

import "golang.org/x/crypto/bcrypt"

// HashPassword 使用 bcrypt 算法对密码进行哈希加密
// cost 参数默认为 14，返回哈希后的密码字符串和错误信息
func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), 14)
	return string(bytes), err
}

// CheckPasswordHash 验证密码与哈希值是否匹配
// 使用 bcrypt 的 CompareHashAndPassword 进行比较
// 返回 true 表示密码正确，false 表示密码错误
func CheckPasswordHash(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}
