package models

import AppContext._
import auth.{UserStatus, SuperUser, NormalUser}
import org.mindrot.jbcrypt.BCrypt
import scala.annotation.switch
import scala.concurrent.Future
import net.fwbrasil.radon.transaction.TransactionalExecutionContext
import play.api.libs.json._

class UserStatusEncoder extends Encoder[UserStatus, Int] {
     
    def encode(status: UserStatus): Int = status match {
       case SuperUser => 1
       case NormalUser => 2
     }

     def decode(value: Int) = (value: @switch) match {
       case 1 => SuperUser
       case 2 => NormalUser
     }
}

object AuthType extends Enumeration {
  	case class AuthType(atype: Int) extends Val(atype)
 
	val PLAIN = AuthType(1)
	val LDAP = AuthType(2)
	val PAM = AuthType(3)
}


case class User(
        val username: String,
	var authType: AuthType.AuthType = AuthType.PLAIN,
	var status: UserStatus,
	var password: Option[String],
	var email: Option[String]=None,
	var fullName: Option[String]=None) extends Entity {

  def toJson = Json.obj(
    "username" -> username,
    "authType" -> authType.toString,
    "status"   -> status.toString,
    "email"    -> email,
    "fullName" -> fullName
  )
}


object User{

  def authenticate(username: String, formPassword: String): Option[User] = transactional(readOnly) {
    select[User].where(_.username :== username) match{
      case user :: Nil if BCrypt.checkpw(formPassword, user.password.get) => Some(user)
      case _ => None
    }
  }

  def createUser(username: String, password: String, authType: AuthType.AuthType = AuthType.PLAIN, status: UserStatus = NormalUser): User =
    transactional(readWrite) {
      User(username, authType, status, Some(BCrypt.hashpw(password, BCrypt.gensalt())))
    }

  def createLDAPUser (
    username: String,
    status: UserStatus = NormalUser, 
    email: Option[String] = None, 
    fullName: Option[String] = None 
    ): User = transactional {
      User(username, AuthType.LDAP, status, None, email, fullName)
  }

  def authenticateLDAPUser (username: String): Option[User] = transactional {
    select[User].where(_.username :== username) match{
      case user::Nil => Some(user)
      case _ => None
    }
  }

  def paginatedListQuery(page: Int = 0, pageSize: Int = 100, orderBy: Int = 1, filter: String = "*")
    (implicit ctx: TransactionalExecutionContext) = {
    val pagination =
            asyncPaginatedQuery {
              (u: User) =>
                    where((toUpperCase(u.username) like filter.toUpperCase) :|| (toUpperCase(u.fullName.get) like filter.toUpperCase)) select (u) orderBy {
                      (orderBy: @switch) match {
                        case -1 => u.username desc
                        case  1 => u.username 
                        case -5 => u.email desc
                        case  5 => u.email
                        case -6 => u.fullName desc
                        case  6 => u.fullName
                      }
                    }
            }

    pagination.navigator(pageSize)
  }

  def list (page: Int = 0, pageSize: Int = 100, orderBy: Int = 1, filter: String = "*")(implicit ctx: TransactionalExecutionContext): Future[Page[User]] = {
    
    paginatedListQuery(page, pageSize, orderBy, filter).flatMap { navigator =>
      if (navigator.numberOfResults > 0)
        navigator.page(page).map((u: Seq[User]) => Page(u, page, page * pageSize, navigator.numberOfResults))
      else
        Future(Page(Nil, 0, 0, 0))
    }
  }

  def listJson(page: Int = 0, pageSize: Int = 100, orderBy: Int = 1, filter: String = "*")(implicit ctx: TransactionalExecutionContext):Future[JsArray] = {
    paginatedListQuery(page, pageSize, orderBy, filter).flatMap { navigator =>
      if (navigator.numberOfResults > 0)
        navigator.page(page).map((u: Seq[User]) => Json.arr(u.map(_.toJson)))
      else
        Future(Json.arr())
    }
  }

}
