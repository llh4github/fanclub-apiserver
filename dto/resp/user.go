package resp

// UserResponse 用户响应数据
type UserResponse struct {
	// 用户ID
	ID int `json:"id,string" swagger:"required,description=用户ID"`
	// 用户名
	Name string `json:"name" swagger:"required,description=用户名"`
	// 邮箱
	Email string `json:"email" swagger:"required,description=邮箱"`
}

// UserListResponse 用户列表响应数据
type UserListResponse struct {
	// 用户列表
	Users []UserResponse `json:"users" swagger:"required,description=用户列表"`
}
