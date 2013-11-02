package auth

sealed trait UserStatus
case object SuperUser extends UserStatus
case object NormalUser extends UserStatus
