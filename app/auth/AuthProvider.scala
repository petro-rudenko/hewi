package auth

import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.util.ssl.{SSLUtil, TrustStoreTrustManager, TrustAllTrustManager}
import jp.t2v.lab.play2.auth.{AuthConfig, AuthElement}
import jp.t2v.lab.play2.stackc.StackableController
import models.{AuthType, User => UserModel}
import models.AppContext._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.classTag


trait AuthConfigImpl extends AuthConfig {
  
  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = String

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = UserModel


  /**
   * A `ClassManifest` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag = classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = play.api.Play.current.configuration.getInt("auth.cookie.timeout").getOrElse(30000000)

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] =
    asyncTransactionalChain{implicit ctx => asyncById[UserModel](id) }   
  
  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] = {
    val uri = request.session.get("access_uri").getOrElse("/config")
    Future.successful(Redirect(uri))
  }

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader)(implicit context: scala.concurrent.ExecutionContext): scala.concurrent.Future[play.api.mvc.SimpleResult] = 
    Future.successful(Redirect(controllers.routes.Application.login))

  /**
   * If the user is not logged in and tries to access a protected resource then redirect them as follows:
   */
  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] = 
    Future.successful(Redirect(controllers.routes.Application.login).withSession(
      "access_uri" ->(if (request.uri != "/") request.uri else "/config")))


  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Forbidden("no permission"))
  

  /**
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
   * TODO: make Https configuration
   */
  override lazy val cookieSecureOption: Boolean = play.api.Play.current.configuration.getBoolean("useHttps").getOrElse(false)

   /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Permission
   * case object Administrator extends Permission
   * case object NormalUser extends Permission
   */
  type Authority = UserStatus
  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    transactional {
      (user.status, authority) match {
	case (SuperUser, _) => true
	case (NormalUser, NormalUser) => true
	case _ => false
      }
    }
  }
}

trait AuthController extends StackableController{
  self: Controller with AuthElement with AuthConfigImpl =>
}

abstract class AbstractAuthProvider{
  def authenticate(username: String, password: String): Option[UserModel]
  def firstUserEver = transactional {all[UserModel].length == 0 }
    
}


object OAuthProvider extends AbstractAuthProvider{
  def authenticate(username: String, password: String): Option[UserModel] = None  
}


object LDAPAuthProvider extends AbstractAuthProvider{
  def authenticate(username: String, formPassword: String): Option[UserModel] = {
    val host = play.api.Play.current.configuration.getString("auth.ldap.host").get
    val port = play.api.Play.current.configuration.getInt("auth.ldap.port").getOrElse(389)
    val bindDN = play.api.Play.current.configuration.getString("auth.ldap.bindDn").get
    val usernamePattern = play.api.Play.current.configuration.getString("auth.ldap.unamePattern").get
    val password = play.api.Play.current.configuration.getString("auth.ldap.password").get //TODO: obfuscation support
    val certFile = play.api.Play.current.configuration.getString("auth.ldap.certificate").getOrElse(None)
    val sslUtil: SSLUtil = certFile match {
      case cert: String => new SSLUtil(new TrustStoreTrustManager(cert))
      case None => new SSLUtil(new TrustAllTrustManager())
    }

    val connection: LDAPConnection = new LDAPConnection(sslUtil.createSSLSocketFactory(), host, port, bindDN, password);

    val entry = connection.getEntry(usernamePattern.replace("<username>", username))

    if (entry == null) return None
    val passwd = entry.getAttributeValue("userPassword")

    if (passwd != formPassword) return None

    val fullName = entry.getAttributeValue("displayName") match {
      case name: String => Some(name)
      case null => None
    }
    val email = entry.getAttributeValue("mail") match {
      case mail: String => Some(mail)
      case null => None        
    }
    
    UserModel.authenticateLDAPUser(username) match {
      case Some(user) => Some(user)
      case None if (firstUserEver) => Some(UserModel.createLDAPUser(username, SuperUser, email, fullName))
      case None => Some(UserModel.createLDAPUser(username, NormalUser, email, fullName))  
    }

  }
}

object DefaultProvider extends AbstractAuthProvider{
  
  def authenticate(username: String, password: String): Option[UserModel] = {  
    if (firstUserEver) Some(UserModel.createUser(username, password, AuthType.PLAIN, SuperUser))
    else UserModel.authenticate(username, password)
  }
}

object AllProviders extends AbstractAuthProvider{
  def authenticate(username: String, password: String): Option[UserModel] = {
    DefaultProvider.authenticate(username, password).orElse(LDAPAuthProvider.authenticate(username, password)) 
  }
}


object AuthProvider{
  def getProvider: AbstractAuthProvider = 
    play.api.Play.current.configuration.getString("auth.default.type").getOrElse("DEFAULT") match {
    	case "LDAP" => LDAPAuthProvider
    	case "OAUTH" => OAuthProvider
    	case "ALL" => AllProviders
    	case "DEFAULT" => DefaultProvider
  }
}



  
