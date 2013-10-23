package models

import AppContext._
import auth.{UserStatus, SuperUser, NormalUser}
import org.mindrot.jbcrypt.BCrypt


class UserStatusEncoder extends Encoder[UserStatus, Int] {
     
    def encode(status: UserStatus) = status match {
       case SuperUser => 1
       case NormalUser => 2
     }

     def decode(value: Int) = value match {
       case 1 => SuperUser
       case 2 => NormalUser
     }
}

object AuthType extends Enumeration {
  	case class AuthType(atype: Int) extends Val(atype)
	val PLAIN = AuthType(1)
	val LDAP = AuthType(2)
	val OAUTH = AuthType(3)
}


case class User(
        val username: String,
	var authType: AuthType.AuthType = AuthType.PLAIN,
	var status: UserStatus,
	//var userStatus: Int = 2,
	var password: Option[String],
	var email: Option[String]=None,
	var firstName: Option[String]=None,
	var lastName: Option[String]=None) extends Entity {

}


object User{

	def authenticate(username: String, formPassword: String): Option[User] = transactional {
	  select[User].where(_.username :== username) match{
	  	case user :: Nil if BCrypt.checkpw(formPassword, user.password.get) => Some(user)
	  	case _ => None
	  }
	}


	def createUser(username: String, password: String, status: UserStatus): User =
	  transactional {
		User(username, status=status, password=Some(BCrypt.hashpw(password, BCrypt.gensalt())))
	  }

}
