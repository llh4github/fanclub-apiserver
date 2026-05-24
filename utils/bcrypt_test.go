package utils

import (
	"testing"
)

func TestHashPass(t *testing.T) {

	hashed, err := HashPassword("12345qaz")
	if err != nil {
		t.Fatalf("HashPassword failed: %v", err)
	}
	t.Logf("Hashed password: \n%s", hashed)
	t.Logf("Hashed length: %d (should be 60)", len(hashed))

	ok := CheckPasswordHash("12345qaz", hashed)
	t.Logf("Verification result: %v", ok)

	if !ok {
		t.Errorf("Hash should match password '12345qaz'")
	}
}

func TestHashPassword(t *testing.T) {
	tests := []struct {
		name     string
		password string
		wantErr  bool
	}{
		{
			name:     "正常密码",
			password: "password123",
			wantErr:  false,
		},
		{
			name:     "空密码",
			password: "",
			wantErr:  false,
		},
		{
			name:     "长密码",
			password: "this_is_a_very_long_password_with_many_characters_123!@#",
			wantErr:  false,
		},
		{
			name:     "中文密码",
			password: "密码测试123",
			wantErr:  false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			hash, err := HashPassword(tt.password)
			if (err != nil) != tt.wantErr {
				t.Errorf("HashPassword() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !tt.wantErr && hash == "" {
				t.Error("HashPassword() returned empty hash")
			}
			// 验证哈希值长度为 60 (bcrypt 标准哈希长度)
			if len(hash) != 60 {
				t.Errorf("HashPassword() hash length = %d, want 60", len(hash))
			}
		})
	}
}

func TestCheckPasswordHash(t *testing.T) {
	// 先哈希一个已知密码
	knownPassword := "testPassword123"
	hash, err := HashPassword(knownPassword)
	if err != nil {
		t.Fatalf("Failed to hash known password: %v", err)
	}

	tests := []struct {
		name     string
		password string
		hash     string
		want     bool
	}{
		{
			name:     "正确密码",
			password: knownPassword,
			hash:     hash,
			want:     true,
		},
		{
			name:     "错误密码",
			password: "wrongPassword",
			hash:     hash,
			want:     false,
		},
		{
			name:     "空密码匹配非空哈希",
			password: "",
			hash:     hash,
			want:     false,
		},
		{
			name:     "非空密码匹配空哈希",
			password: "somePassword",
			hash:     "",
			want:     false,
		},
		{
			name:     "部分匹配的密码",
			password: "testPassword",
			hash:     hash,
			want:     false,
		},
		{
			name:     "大小写不同的密码",
			password: "TESTPASSWORD123",
			hash:     hash,
			want:     false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := CheckPasswordHash(tt.password, tt.hash); got != tt.want {
				t.Errorf("CheckPasswordHash() = %v, want %v", got, tt.want)
			}
		})
	}
}

func TestHashAndCheckWorkflow(t *testing.T) {
	password := "integrationTestPassword"
	hash, err := HashPassword(password)
	if err != nil {
		t.Fatalf("HashPassword() failed: %v", err)
	}

	// 验证哈希后能正确校验
	if !CheckPasswordHash(password, hash) {
		t.Error("HashAndCheckWorkflow: password should match its hash")
	}

	// 验证不同密码不匹配
	if CheckPasswordHash(password+"x", hash) {
		t.Error("HashAndCheckWorkflow: modified password should not match hash")
	}
}
