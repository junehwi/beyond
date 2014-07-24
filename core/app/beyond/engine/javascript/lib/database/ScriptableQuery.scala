package beyond.engine.javascript.lib.database

import beyond.engine.javascript.BeyondContextFactory
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.annotations.JSFunction
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument

object ScriptableQuery {
  // Cannot find eq method when @JSFunction annotation is used
  // because static forwarder is not generated when the same name method exists in the class and companion object.
  def jsFunction_eq(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> value)
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def neq(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> BSONDocument("$ne" -> value))
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def lt(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> BSONDocument("$lt" -> value))
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def lte(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> BSONDocument("$lte" -> value))
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def gt(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> BSONDocument("$gt" -> value))
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def gte(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val field = args(0).asInstanceOf[String]
    val value = args(1)
    val newQuery: BSONDocument = currentQuery.add(field -> BSONDocument("$gte" -> value))
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def where(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery =
    ???

  @JSFunction
  def or(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val orQuery = BSONArray(args.map(_.asInstanceOf[ScriptableQuery].query).+:(currentQuery))
    val newQuery: BSONDocument = BSONDocument("$or" -> orQuery)
    ScriptableQuery(context, newQuery)
  }

  @JSFunction
  def and(context: Context, thisObj: Scriptable, args: Array[AnyRef], function: Function): ScriptableQuery = {
    val currentQuery: BSONDocument = thisObj.asInstanceOf[ScriptableQuery].query
    val andQuery: BSONDocument = args.map(_.asInstanceOf[ScriptableQuery].query).fold(currentQuery) {
      case (currentDocument, newDocument) => currentDocument.add(newDocument)
    }
    ScriptableQuery(context, andQuery)
  }

  def jsConstructor(context: Context, args: Array[AnyRef], constructor: Function, inNewExpr: Boolean): ScriptableQuery =
    args match {
      case Array() => new ScriptableQuery
      case Array(bson: BSONDocument) => new ScriptableQuery(bson)
      case _ => throw new IllegalArgumentException
    }

  private[database] def apply(context: Context, bson: BSONDocument): ScriptableQuery = {
    val beyondContextFactory = context.getFactory.asInstanceOf[BeyondContextFactory]
    val scope = beyondContextFactory.global
    val args: Array[AnyRef] = Array(bson)
    context.newObject(scope, "Query", args).asInstanceOf[ScriptableQuery]
  }
}

class ScriptableQuery(val query: BSONDocument) extends ScriptableObject {
  def this() = this(BSONDocument.empty)

  override val getClassName: String = "Query"
}
