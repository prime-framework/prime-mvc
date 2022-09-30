/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.primeframework.mvc.parameter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that a property should be unwrapped, this means the contents will be flattened in the
 * declaring class.
 * <p/>
 * If two fields are annotated with this annotation and the objects both have a field named the same the results are not
 * defined. For example, in the follow example, <code>foo.name</code> is ambiguous when flattened, the field exists in
 * <code>Bar</code> and <code>Baz</code>. During traversal, the first match will be used, doing this is not
 * recommended.
 * <p>
 * <pre>
 *   public class Example {
 *
 *     public Foo foo;
 *
 *     public static class Foo {
 *      {@literal @}FieldUnwrapped
 *       public Bar bar;
 *
 *      {@literal @}FieldUnwrapped
 *       public Baz baz;
 *     }
 *
 *     public static class Bar {
 *       public String name;
 *     }
 *
 *     public static class Baz {
 *       public String name;
 *     }
 *   }
 * </pre>
 *
 * @author Daniel DeGroff
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldUnwrapped {
}
