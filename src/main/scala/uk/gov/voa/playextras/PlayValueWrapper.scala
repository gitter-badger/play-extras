/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.voa.playextras

import play.api.libs.json._
import play.api.mvc.{PathBindable, QueryStringBindable}

/**
 * This trait defines implicits for the common Play Framework transformations on values. Namely:
 * - A Reads[W] to read from json
 * - A Writes[W] to write to json
 * - A PathBindable to convert to and from strings in the url pattern
 * - A QueryStringBindable to convert to and from url query parameters
 *
 * To use this with, for instance, a case class that extends AnyVal, create a companion object
 * that extends this trait with V being the primitive type (e.g. String, Int) and W being the
 * wrapped type. Then make an implicit instance of ValueWrapper[V,W] within the companion
 * object, defining the appropriate wrap and unwrap methods.
 */
trait PlayValueWrapper[V, W] {
  implicit def reads(implicit vr: Reads[V], w: ValueWrapper[V, W]): Reads[W] =
    PlayValueWrapper.reads[V, W]

  implicit def writes(implicit vw: Writes[V], w: ValueWrapper[V, W]): Writes[W] =
    PlayValueWrapper.writes[V, W]

  implicit def pathBindable(implicit pb: PathBindable[V], vw: ValueWrapper[V, W]): PathBindable[W] =
    PlayValueWrapper.pathBindable[V, W]

  implicit def queryStringBinder(implicit qb: QueryStringBindable[V], vw: ValueWrapper[V, W]): QueryStringBindable[W] =
    PlayValueWrapper.queryStringBindable[V, W]
}

/**
 * Provide functions to create various Play type class instances for types
 * that implement a ValueWrapper
 */
object PlayValueWrapper {
  def writes[V: Writes, W](implicit vw: ValueWrapper[V, W]): Writes[W] =
    new Writes[W] {
      override def writes(o: W): JsValue = implicitly[Writes[V]].writes(vw.unwrap(o))
    }

  def reads[V: Reads, W](implicit vw: ValueWrapper[V, W]): Reads[W] =
    new Reads[W] {
      override def reads(json: JsValue): JsResult[W] = implicitly[Reads[V]].reads(json).flatMap { v =>
        vw.wrap(v) match {
          case Right(w) => JsSuccess(w)
          case Left(e) => JsError(e)
        }
      }
    }

  def formats[V: Reads : Writes, W](implicit vw: ValueWrapper[V, W]): Format[W] = new Format[W] {
    override def writes(o: W): JsValue = PlayValueWrapper.writes[V, W].writes(o)

    override def reads(json: JsValue): JsResult[W] = PlayValueWrapper.reads[V, W].reads(json)
  }

  def pathBindable[V, W](implicit vb: PathBindable[V], vw: ValueWrapper[V, W]): PathBindable[W] =
    new PathBindable[W] {
      override def unbind(key: String, w: W): String = vb.unbind(key, vw.unwrap(w))

      override def bind(key: String, v: String): Either[String, W] =
        vb.bind(key, v).right.flatMap(vw.wrap)
    }

  def queryStringBindable[V, W](implicit vb: QueryStringBindable[V], vw: ValueWrapper[V, W]): QueryStringBindable[W] =
    new QueryStringBindable[W] {
      override def unbind(key: String, w: W): String = vb.unbind(key, vw.unwrap(w))

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, W]] =
        vb.bind(key, params).map(_.right.flatMap(vw.wrap))
    }
}
