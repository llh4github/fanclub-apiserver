package consts

type RoleType string

const (
	RoleAdmin  RoleType = "ADMIN"
	RoleAnchor RoleType = "ANCHOR"
	RoleGuest  RoleType = "GUEST"
)

func ConvertRoleType(role string) RoleType {
	switch role {
	case "ADMIN":
		return RoleAdmin
	case "ANCHOR":
		return RoleAnchor
	default:
		return RoleGuest
	}
}

func (r RoleType) IsAdmin() bool {
	return r == RoleAdmin
}

func (r RoleType) IsAnchor() bool {
	return r == RoleAnchor
}
