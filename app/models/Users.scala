package models

import AppContext._
import auth.{UserStatus, SuperUser, NormalUser}
import org.mindrot.jbcrypt.BCrypt


object AuthType extends Enumeration {
  	case class AuthType(atype: Int) extends Val(atype)
	val PLAIN = AuthType(1)
	val LDAP = AuthType(2)
	val OAUTH = AuthType(3)
}


case class User(
    val username: String, 
	var status: Int,
	var authType: AuthType.AuthType = AuthType.PLAIN,
	//var authType: Int = 1,
	var password: Option[String],
	var email: Option[String]=None,
	var firstName: Option[String]=None,
	var lastName: Option[String]=None) extends Entity {
  
  /**
   * Temporary hack while Activate framework doesn't support custom type serialization.
   */
  def getUserStatus = transactional{ 
  status match{
    case 1 => SuperUser
    case 2 => NormalUser
  	}
  }
}
	
	
object User{
  
	def authenticate(username: String, formPassword: String): Option[User] = transactional {
	  select[User].where(_.username :== username) match{ 
	  	case (user @ User(_,_,_,password,_,_,_)) :: Nil if BCrypt.checkpw(formPassword, password.get) => Some(user)
	  	case _ => None
	  }
	}
	
	/**
	 * Temporary hack while Activate framework doesn't support custom type serialization.
	 */
	def setUserStatus(status: UserStatus) = status match{
	  case SuperUser => 1
	  case NormalUser => 2
	}
	  	  
		
	def createUser(username: String, password: String, status: UserStatus): User = 
	  transactional {
		User(username, setUserStatus(status), password=Some(BCrypt.hashpw(password, BCrypt.gensalt())))
	  }
	
}