package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._

import scala.util.matching.Regex

/**
 * Handle a CRUD for persons phone book
 */
object Application extends Controller { 
  
  /** Redirect to the application home. */
  val Home = Redirect(routes.Application.list(0, 2, ""))

  /** Person form for edit and create screens. */ 
  val personForm = Form(
    mapping(
      "id" -> ignored(None:Option[Long]),
      "name" -> nonEmptyText,
      "phone" -> nonEmptyText.verifying("error.digitsonly", validatePhone(_))
    )(Person.apply)(Person.unapply).verifying(
        "The Name and Phone already exists", person => 
        Person.findByName(person.name).isEmpty && Person.findByPhone(person.phone).isEmpty )
  )

  /** Handle person phone validation */
  def validatePhone(phone: String): Boolean = {
    val regex: Regex = "^([0-9])*".r
    regex.pattern.matcher(phone).matches()
  }

  /** Handle default path requests, redirect to person list */  
  def index = Action { Home }
  
  /**
   * Display the paginated list of persons.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on person names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.list(
      Person.list(page = page, orderBy = orderBy, filter = "%"+filter+"%"),
      orderBy, filter
    ))
  }
  
  /**
   * Display the 'edit form' of a existing Person.
   *
   * @param id Id of the person to edit
   */
  def edit(id: Long) = Action {
    Person.findById(id).map { person =>
      Ok(html.editForm(id, personForm.fill(person)))
    }.getOrElse(NotFound)
  }
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the person to edit
   */
  def update(id: Long) = Action { implicit request =>
    personForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors)),
      person => {
        Person.update(id, person)
        Home.flashing("success" -> "Person %s has been updated".format(person.name))
      }
    )
  }
  
  /** Display the 'new person form'. */
  def create = Action {
    Ok(html.createForm(personForm))
  }
  
  /** Handle the 'new person form' submission. */
  def save = Action { implicit request =>
    personForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createForm(formWithErrors)),
      person => {
        Person.insert(person)
        Home.flashing("success" -> "Person %s has been created".format(person.name))
      }
    )
  }
  
  /** Handle person deletion. */
  def delete(id: Long) = Action {
    Person.delete(id)
    Home.flashing("success" -> "Person has been deleted")
  }
}
