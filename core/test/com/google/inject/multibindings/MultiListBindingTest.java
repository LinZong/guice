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

import com.google.inject.*;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the various @Ordered annotations.
 *
 * @author linshengyi123@hotmail.com (Nemesiss Lin)
 */
public class MultiListBindingTest extends TestCase {

    interface Shape {
        String getName();
    }

    @Ordered(1)
    static class Circle implements Shape {

        @Override
        public String getName() {
            return "circle";
        }
    }

    static class Square implements Shape {

        @Override
        public String getName() {
            return "square";
        }
    }

    @Ordered(2)
    static class SquareProvider implements Provider<Square> {

        @Override
        public Square get() {
            return new Square();
        }
    }

    @Ordered(-1000)
    abstract static class AbstractRectangle implements Shape {
        protected int width;
        protected int height;
    }

    // @Ordered annotation is in super class.
    static class RectangleImpl extends AbstractRectangle {

        public RectangleImpl() {
            this.width = 114;
            this.height = 514;
        }

        @Override
        public String getName() {
            return String.format("rectangle[width=%d, height=%d]", width, height);
        }
    }

    // no @Ordered annotation.
    static class Triangle implements Shape {

        @Override
        public String getName() {
            return "triangle";
        }
    }

    static class ShapeStore {

        @Inject
        List<Shape> shapes;

        void verify() {
            assertEquals(5, shapes.size());
            assertEquals(String.format("rectangle[width=%d, height=%d]", 114, 514), shapes.get(0).getName());
            assertEquals("circle", shapes.get(1).getName());
            assertEquals("square", shapes.get(2).getName());
            assertEquals("square", shapes.get(3).getName());
            assertEquals("triangle", shapes.get(4).getName());
        }
    }


    static class ShapeModules extends AbstractModule {

        @Override
        protected void configure() {
            super.configure();
            bind(ShapeStore.class).in(Singleton.class);
            Multibinder<Shape> shapeListBinder = Multibinder.newListBinder(binder(), Shape.class);
            // 1
            shapeListBinder.addBinding().to(Circle.class).in(Singleton.class);
            // 2
            shapeListBinder.addBinding().toProvider(SquareProvider.class).in(Singleton.class);
            // 2
            shapeListBinder.addBinding().toProvider(new SquareProvider()).in(Singleton.class);
            // -1000
            shapeListBinder.addBinding().to(RectangleImpl.class).in(Singleton.class);

            shapeListBinder.addBinding().to(Triangle.class).in(Singleton.class);
        }
    }


    public void testMultiListBinding() {
        Injector injector = Guice.createInjector(new ShapeModules());
        ShapeStore shapeStore = injector.getInstance(ShapeStore.class);
        shapeStore.verify();
    }
}
