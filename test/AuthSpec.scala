package test

import com.unboundid.util.ssl._
import com.unboundid.ldap.listener._
import java.io.File
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._
import models.AppContext._
import auth._

class AuthSpec extends Specification {

  def startLDAP: InMemoryDirectoryServer = {

    val port = play.api.Play.current.configuration.getInt("auth.ldap.port").getOrElse(3890)
    val bindDN = play.api.Play.current.configuration.getString("auth.ldap.bindDn").get
    val password = play.api.Play.current.configuration.getString("auth.ldap.password").get //TODO: obfuscation support
    val certFile = play.api.Play.current.configuration.getString("auth.ldap.certificate").get

    val config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
    config.addAdditionalBindCredentials(bindDN, password);

    val serverKeyStore = new File(certFile);
    val serverSSLUtil: SSLUtil = new SSLUtil(
      new KeyStoreKeyManager(serverKeyStore, "password".toCharArray(),
        "JKS", "server-cert"),
      new TrustAllTrustManager());
    val clientSSLUtil: SSLUtil = new SSLUtil(new TrustAllTrustManager());

    config.setListenerConfigs(
      InMemoryListenerConfig.createLDAPConfig("LDAP", 0),
      InMemoryListenerConfig.createLDAPSConfig(
        "LDAPS",
        null, port, serverSSLUtil.createSSLServerSocketFactory(),
        clientSSLUtil.createSSLSocketFactory()));

    config.setSchema(null); // do not check (attribute) schema
    val server = new InMemoryDirectoryServer(config);
    server.importFromLDIF(false, "test/resources/users_groups.ldif");
    server.startListening();

    server
  }

  "Application" should {

    "Login first user as superuser with any username/password" in new WithApplication {
      try { transactional(readWrite) { all[User].foreach(_.delete) } } // https://groups.google.com/forum/#!searchin/activate-persistence/activatetest/activate-persistence/I0sHxv4WatI/l1mw2bAJDdcJ
      val users = transactional(readOnly) { all[User] }
      users must be empty
      val superUser = AuthProvider.getProvider.authenticate("test", "1111").get
      (transactional(readOnly) { superUser.status }) must be(SuperUser)

      AuthProvider.getProvider.authenticate("user2", "1111") must be(None)
      val user3 = AuthProvider.getProvider.authenticate("test", "1111").get
      user3 must be(superUser)
    }

    "Support LDAP Auth" in new WithApplication {
      try { transactional(readWrite) { all[User].foreach(_.delete) } } // https://groups.google.com/forum/#!searchin/activate-persistence/activatetest/activate-persistence/I0sHxv4WatI/l1mw2bAJDdcJ
      val server = startLDAP
      val superUserOption = LDAPAuthProvider.authenticate("testAdminUser", "testPassword")
      superUserOption must beSome
      server.shutDown(true)
    }

  }

}
