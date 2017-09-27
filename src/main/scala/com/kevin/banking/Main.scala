package com.kevin.banking

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.util.Timeout


import scala.concurrent.duration._
import scala.io.StdIn

object Main  extends App {


  implicit val actorSystem = ActorSystem("bank")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val host = actorSystem.settings.config.getString("http.host") // Gets the host and a port from the configuration
  val port = actorSystem.settings.config.getInt("http.port")

  import Bank._

  val bank = actorSystem.actorOf(Bank.props, "bank-actor")

  val route: Route = {

    implicit val timeout = Timeout(5 seconds)

    path("balance") {
      get {
        onSuccess(bank ? CheckBalance) {
          case Balance(amount) =>
            complete(StatusCodes.OK,s"Account Balance is:  $amount.")
          case _ =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }



  val bindingFuture = Http().bindAndHandle(route, host, port)
  println(s"Waiting for requests at http://$host:$port/...\n Hit RETURN to terminate.")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind())
  actorSystem.terminate()
}
