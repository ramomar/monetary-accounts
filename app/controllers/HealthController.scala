package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class HealthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def health = Action { implicit request: Request[AnyContent] =>
    Ok("(:")
  }
}
