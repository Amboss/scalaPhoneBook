package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

/** Person model. */
case class Person(id: Option[Long] = None, name: String, phone: String)

/** Page pagination. */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

/**
 * Object for Person service
 */
object Person {
   
  /** Parse a Person from a ResultSet */
  val simple = {
    get[Option[Long]]("person.id") ~
    get[String]("person.name") ~
    get[String]("person.phone") map {
      case id~name~phone => Person(id, name, phone)
    }
  }

  /** 
   * Retrieve a person from the id.
   * 
   * @param id Id of the person to select
   */
  def findById(id: Long): Option[Person] = {
    DB.withConnection { implicit connection =>
      SQL("select * from person where id = {id}").on('id -> id).as(Person.simple.singleOpt)
    }
  }
  
  /** 
   * Retrieve a person from the name.
   * 
   * @param name of the person to select
   */
  def findByName(name: String): Option[Person] = {
    DB.withConnection { implicit connection =>
      SQL("select * from person where name = {name}").on('name -> name).as(Person.simple.singleOpt)
    }
  }
  
  /** 
   * Retrieve a person from the phone.
   * 
   * @param phone of the person to select
   */
  def findByPhone(phone: String): Option[Person] = {
    DB.withConnection { implicit connection =>
      SQL("select * from person where phone = {phone}").on('phone -> phone).as(Person.simple.singleOpt)
    }
  }
  
  /**
   * Return a page of Person.
   *
   * @param page Page to display
   * @param pageSize Number of personns per page
   * @param orderBy Person property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 1, filter: String = "%"): Page[Person] = {
    
    val offest = pageSize * page
    
    DB.withConnection { implicit connection =>
      val persons = SQL("""select * from person where person.name like {filter}
          order by {orderBy} nulls last limit {pageSize} offset {offset}""")
          .on('pageSize -> pageSize, 'offset -> offest, 'filter -> filter,
          'orderBy -> orderBy).as(simple *)
      val totalRows = SQL("""select count(*) from person where person.name like {filter}""")
        .on('filter -> filter).as(scalar[Long].single)
      Page(persons, page, offest, totalRows)
    }
  }
  
  /**
   * Update a person.
   *
   * @param id The person id
   * @param person The person values.
   */
  def update(id: Long, person: Person) = {
    DB.withConnection { implicit connection =>
      SQL("""update person set name = {name}, phone = {phone} where id = {id}""")
        .on('id -> id,'name -> person.name,'phone -> person.phone).executeUpdate()
    }
  }
  
  /**
   * Insert a new person.
   *
   * @param person The person values.
   */
  def insert(person: Person) = {
    DB.withConnection { implicit connection =>
      SQL("""insert into person values ((select next value for person_seq),{name}, {phone})"""
      ).on('name -> person.name,'phone -> person.phone).executeUpdate()
    }
  }
  
  /**
   * Delete a person.
   *
   * @param id Id of the person to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from person where id = {id}").on('id -> id).executeUpdate()
    }
  }
}