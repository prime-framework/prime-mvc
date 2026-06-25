/*
 * Copyright (c) 2026, Inversoft Inc., All Rights Reserved
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

package org.primeframework.mvc.util;

import org.primeframework.mvc.util.ClassClasspathResolver.AnnotatedWith;
import org.primeframework.mvc.util.ClassClasspathResolver.IsA;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class ClassClasspathResolverTest {
    @Test
    public void findByLocators_annotated_matches_leaf_package_recursive_on() throws IOException {
        // Use case: A class with the desired annotation exists and is in the right package, we should see it in the results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                true,
                // util is the leaf package, so should match whether recursive is on or off
                "util");

        // assert
        assertEquals(results,
                Set.of(Matching.class));
    }

    @Test
    public void findByLocators_annotated_matches_leaf_package_recursive_off() throws IOException {
        // Use case: A class with the desired annotation exists and is in the right package, we should see it in the results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                false,
                // util is the leaf package, so should match whether recursive is on or off
                "util");

        // assert
        assertEquals(results,
                Set.of(Matching.class));
    }

    @Test
    public void findByLocators_annotated_matches_recursive_intermediary_package() throws IOException {
        // Use case: A class with the desired annotation exists and is in the right package, we should see it in the results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                true,
                // mvc is an intermediary package, should match
                "mvc");

        // assert
        assertEquals(results,
                Set.of(Matching.class));
    }

    @Test
    public void findByLocators_annotated_matches_no_recursive() throws IOException {
        // Use case: A class with the desired annotation exists and is in an intermediary package, but recursive
        //           is off, so we should not have any results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                false,
                "mvc");

        // assert
        assertEquals(results,
                Set.of());
    }

    @Test
    public void findByLocators_no_class_annotated_no_match() throws IOException {
        // Use case: A class with the desired annotation exists and is in an intermediary package, but recursive
        //           is off, so we should not have any results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(NonMatchingAnnotation.class),
                false,
                "mvc");

        // assert
        assertEquals(results,
                Set.of());
    }

    @Test
    public void findByLocators_annotated_wrong_locator_no_match() throws IOException {
        // Use case: A class with the desired annotation exists but the locator does not match the package it's in
        //           so we should have empty results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                true,
                "foobar");

        // assert
        assertEquals(results,
                Set.of());
    }

    @Test
    public void findByLocators_annotated_no_locator() throws IOException {
        // Use case: A class with the desired annotation exists, but no locator is provided and without a locator
        //           we should return no results

        // arrange
        ClassClasspathResolver<Object> resolver = new ClassClasspathResolver<>();

        // act
        Set<? extends Class<?>> results = resolver.findByLocators(new AnnotatedWith<>(MatchingAnnotation.class),
                true);

        // assert
        assertEquals(results,
                Set.of());
    }

    @Test
    public void findByLocators_is_a_no_match() throws IOException {
        // Use case: A class with IsA is provided a class and no matching class exists in the package

        // arrange
        ClassClasspathResolver<String> resolver = new ClassClasspathResolver<>();

        // act
        Set<Class<String>> results = resolver.findByLocators(new IsA<>(String.class),
                true,
                "util");

        // assert
        assertEquals(results,
                Set.of());
    }

    @Test
    public void findByLocators_is_a_exact_match() throws IOException {
        // Use case: A class with IsA is provided a class. 2 classes that ultimately extend from the supplied class are returned in the results

        // arrange
        ClassClasspathResolver<Matching> resolver = new ClassClasspathResolver<>();

        // act
        Set<Class<Matching>> results = resolver.findByLocators(new IsA<>(Matching.class),
                true,
                "util");

        // assert
        assertEquals(results,
                Set.of(Inherit.class, InheritGrandchild.class));
    }

    // other matchers

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface MatchingAnnotation {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface NonMatchingAnnotation {

    }

    @MatchingAnnotation
    public static class Matching {
    }

    public static class Inherit extends Matching {

    }

    public static class InheritGrandchild extends Inherit {

    }
}
