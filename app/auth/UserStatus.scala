package auth

sealed trait UserStatus extends Serializable
case object SuperUser extends UserStatus
case object NormalUser extends UserStatus