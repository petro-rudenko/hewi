package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._
import models.HewiContext._
import auth._

class LoginSpec extends Specification {
  "Application" should {
           
    "Login first user as superuser with any username/password" in new WithApplication{
      val users = transactional { all[User]}
      (users must be).empty
      val superUser = AuthProvider.getProvider.authenticate("test", "1111").get
      transactional {superUser.status} must be (SuperUser)
      
      AuthProvider.getProvider.authenticate("user2", "1111") must be (None)
      val user3 = AuthProvider.getProvider.authenticate("test", "1111").get
      user3 must be (superUser)
    }
    
  }
}