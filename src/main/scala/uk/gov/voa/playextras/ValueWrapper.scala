/*
 * Copyright 2015 Valuation Office Agency
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

/**
 * Define a type class that can take a value of type V and wrap it in an
 * instance of type W, and take a W and unwrap it to a V
 *
 * For example, if W is `case class Foo(value:String) extends AnyVal` then
 * we can create an instance of ValueWrapper as:
 *
 * ```implicit val FooValueWrapper extends ValueWrapper[String, Foo] {
 * def wrap(v:String) : Either[String, Foo] = Right(Foo(v))
 * def unwrap(f:Foo) = foo.value
 * }```
 *
 * The `wrap` method returns `Either[String, W]` to allow for situations where
 * the V being passed in cannot be converted to a W.
 *
 * By making this a type class we can re-use instances of it in a number of
 * places, for example to remove a great deal of boilerplace when creating
 * instances of Play Framework json formatters and url path binders for
 * case classes that extend AnyVal
 *
 * @tparam V the type of the value that is being wrapped
 * @tparam W the type of the wrapper instance
 */
trait ValueWrapper[V, W] {
  /*
  * @return a Right with the wrapped value, or a Left with an error message if
  * this value cannot be wrapped
   */
  def wrap(v: V): Either[String, W]

  def unwrap(w: W): V
}




