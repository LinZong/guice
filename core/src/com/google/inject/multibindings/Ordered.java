/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.multibindings;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows users define customized order of elements in list provided by list bindings
 * The custom key annotation can ONLY be applied to implementation class.
 *
 * <br />
 *
 * Example:
 *
 * <pre>
 * public interface Shape {
 *     String getName();
 * }
 *
 *{@literal @Ordered(100)}
 * public class Circle implements Shape {
 *     public String getName() { return "circle"; }
 * }
 *
 *{@literal @Ordered(200)}
 * public class Rectangle implements Shape {
 *     public String getName() { return "rectangle"; }
 * }
 *
 * public class ShapeFactory {
 *    {@literal @}{@link com.google.inject.Inject}
 *    {@literal List<Shape> shapes;}
 *
 *    public void verify() {
 *        assertEquals(2, shapes.size());
 *        assertEquals("circle", shapes.get(0).getName());
 *        assertEquals("rectangle", shapes.get(0).getName());
 *    }
 * }
 * </pre>
 *
 * @since 4.0
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Ordered {

    /**
     * Implementation class which has smaller {@link  Ordered} value takes prior to larger or non-annotated one.
     */
    int value() default Integer.MAX_VALUE;
}
