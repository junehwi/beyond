package beyond

import akka.actor.ActorRef
import akka.actor.Props
import beyond.metrics.RequestsCountFilter
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.{ StrictLogging => Logging }
import java.io.File
import play.api.Application
import play.api.Configuration
import play.api.Mode
import play.api.Play
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.mvc.Results.NotFound
import scala.concurrent.Future
import scalax.file.Path

object Global extends WithFilters(RequestsCountFilter, TimeoutFilter) with Logging {
  private var beyondSupervisor: Option[ActorRef] = _

  override def onLoadConfig(defaultConfig: Configuration, path: File, classLoader: ClassLoader, mode: Mode.Mode): Configuration = {
    def loadConfigFromFile(configName: String): Configuration = {
      val file = new File(path, s"conf/$configName")
      if (file.exists) {
        logger.info(s"Load ${file.getCanonicalPath}")
        Configuration(ConfigFactory.parseFile(file))
      } else {
        Configuration.empty
      }
    }
    val modeSpecificConfiguration = loadConfigFromFile(s"application.${mode.toString.toLowerCase}.conf")

    val finalConfiguration = defaultConfig ++ modeSpecificConfiguration
    super.onLoadConfig(finalConfiguration, path, classLoader, mode)
  }

  override def beforeStart(app: Application) {
    super.beforeStart(app)

    val nativeLib = Path.fromString(app.path.getAbsolutePath) / "core" / "target" / "native_libraries" / (System.getProperty("sun.arch.data.model") + "bits")
    val defaultLibPath = System.getProperty("java.library.path")
    val newLibPath = defaultLibPath + File.pathSeparator + nativeLib.path
    System.setProperty("java.library.path", newLibPath)
  }

  override def onStart(app: Application) {
    logger.info("Beyond started")
    BeyondMBean.register()
    beyondSupervisor = Some(Akka.system(app).actorOf(Props[BeyondSupervisor], name = BeyondSupervisor.Name))
  }

  override def onStop(app: Application) {
    logger.info("Beyond stopped")
    beyondSupervisor.foreach(Akka.system(app).stop)
    beyondSupervisor = None
    BeyondMBean.unregister()
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Play.maybeApplication.filter(_.mode == Mode.Prod).map { _ =>
      Future.successful(NotFound)
    } getOrElse {
      super.onHandlerNotFound(request)
    }
  }
}

