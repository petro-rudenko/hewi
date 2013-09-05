/*package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import auth.UserStatus
import org.mindrot.jbcrypt.BCrypt

object AuthType extends Enumeration {
	val PLAIN = Value(1)
	val LDAP = Value(2)
	val OAUTH = Value(3)
}

case class User(id: Option[Long],
				username:String,
				status: UserStatus,
				authType: AuthType.Value = AuthType.PLAIN,
				password: Option[String],
				email: Option[String]=None,
				firstName: Option[String]=None,
				lastName: Option[String]=None)

object Users extends Table[User]("Users"){
		
		implicit val authTypeMapper = MappedTypeMapper.base[AuthType.Value, Int](_.id, AuthType(_))
		implicit val userStatusMapper = MappedTypeMapper.base[UserStatus, String](
		    UserStatus.getValue(_), UserStatus.valueOf(_)
		)
  
		def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
		def username = column[String]("USERNAME")
		def usernameIndex = index("uname_idx", username, unique=true)
		def status = column[UserStatus]("STATUS")
		def authType = column[AuthType.Value]("AUTH_TYPE")
		def password = column[Option[String]]("PASSWORD")
		def email = column[Option[String]]("EMAIL")
		def firstName = column[Option[String]]("FIRST_NAME")
		def lastName = column[Option[String]]("LAST_NAME")
		
		def * = id.? ~ username ~ status ~ authType ~ password ~ email ~ firstName ~ lastName <>
																(User.apply _, User.unapply _)
		def autoInc = * returning id
															
		def findById(id:Long): Option[User] = DB.withSession {
			implicit session => 
			  Query(Users).filter(_.id === id).firstOption			 
		}
															
		def findByUsername(username: String): Option[User] = DB.withSession {
			implicit session => Query(Users).filter(_.username === username).firstOption			 
		}
		
		def count: Int = DB.withSession {
		  implicit session => Query(Users.length).first
		}
															
		def authenticate(username: String, password: String): Option[User] = DB.withSession {
		  implicit session =>
			findByUsername(username).filter{ user => BCrypt.checkpw(password, user.password.get) }
		}
		
		def createUser(username: String, password: String, status: UserStatus): User = DB.withSession {
		  implicit session => 
		    val user = User(id=None, username=username, status=status, password=Some(BCrypt.hashpw(password, BCrypt.gensalt())))
		    val uid = Users.autoInc.insert(user)
		    user.copy(id = Some(uid))
		}
		  
}
*/
