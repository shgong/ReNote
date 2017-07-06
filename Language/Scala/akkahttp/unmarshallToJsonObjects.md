Your code should be okay provided you have the right implicits in scope. If you have an implicit FlowMaterializer in scope then things should work as expected as this code that compiles shows:

```
import akka.http.scaladsl.server.Route
import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.FlowMaterializer

implicit val system = ActorSystem("test")
implicit val mater = ActorFlowMaterializer()

val routes:Route = {
  post{
    decodeRequest{
      entity(as[String]){ str =>
        complete(OK, str) 
      }
    }
  }    
}
```

If you wanted to take things a step further and unmarshall to a JsObject then you just need an implicit Unmarshaller in scope to handle that conversion, something like this:

```
implicit val system = ActorSystem("test")
implicit val mater = ActorFlowMaterializer()

import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.HttpEntity

implicit val um:Unmarshaller[HttpEntity, JsObject] = {
  Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
    Json.parse(data.toArray).as[JsObject]
  }    
}  

val routes:Route = {
  post{
    decodeRequest{
      entity(as[String]){ str =>
        complete(OK, str) 
      }
    }
  } ~
  (post & path("/foo/baz") & entity(as[JsObject])){ baz =>
    complete(OK, baz.toString)
  }    
}
```
