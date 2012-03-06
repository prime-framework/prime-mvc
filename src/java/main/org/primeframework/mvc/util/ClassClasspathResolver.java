/*
 * Copyright (c) 2001-2007, Inversoft, All Rights Reserved
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static java.util.Arrays.*;

/**
 * Locates classes within the current ClassLoader/ClassPath. This class begins from a directory and locates all the
 * <strong>.class</strong> files in that directory and possibly in sub-directories. For each file located either on the
 * file system or in a JAR file, the classes are loaded into the JVM as Class objects. These objects are then passed to
 * the various Test classes and interfaces defined in this class.
 * <p/>
 * When Class instances are matched using the Test interfaces and classes from this class that are added to a set of
 * matches. These matches can then be fetched and used however is required.
 *
 * @author Brian Pontarelli
 */
public class ClassClasspathResolver<U> {

  /**
   * Attempts to discover resources that pass the test.
   * <p/>
   * Examples:
   * <p/>
   * <pre>
   * locators: foo bar
   *
   * JAR file in classpath
   * ----------------------
   * com/example/foo/sub/File.class
   *
   * Directory in classpath (/opt/classpath)
   * ----------------------
   * /opt/classpath/com/example/bar/sub/File2.class
   *
   * This will find directories com/example/foo and com/example/bar
   * </pre>
   *
   * @param test       The test implementation to determine matching resources.
   * @param recursive  If true, this will recurse into sub-directories. If false, this will only look in the directories
   *                   given.
   * @param locators   A list of directory locators that are used to locate directories to find resources in.
   * @return The matching set.
   * @throws IOException If there was any errors while inspecting the classpath.
   */
  public Set<Class<U>> findByLocators(Test<Class<U>> test, boolean recursive, String... locators)
    throws IOException {
    if (locators == null) {
      return null;
    }

    Classpath classpath = Classpath.getCurrentClassPath();
    List<String> names = classpath.getNames();
    Set<Class<U>> matches = new HashSet<Class<U>>();
    for (String name : names) {
      File f = new File(name);
      if (f.isDirectory()) {
        for (String locator : locators) {
          File dir = findFirstDirectory(f, locator);
          if (dir != null) {
            matches.addAll(loadFromDirectory(dir, test, recursive));
          }
        }
      } else {
        matches.addAll(loadFromJar(f, test, recursive, asList(locators), true));
      }
    }

    return matches;
  }

  private File findFirstDirectory(File dir, String locator) {
    // Loop over the files
    Queue<File> files = new LinkedList<File>(safeListFiles(dir));
    while (!files.isEmpty()) {
      File file = files.poll();
      if (file.isDirectory() && file.getName().equals(locator)) {
        return file;
      } else if (file.isDirectory()) {
        files.addAll(safeListFiles(file));
      }
    }

    return null;
  }

  /**
   * This provides a safe mechanism for listing all of the files in a directory. The listFiles method can return null
   * and cause major issues. This performs that method in a null safe manner.
   *
   * @param dir    The directory to list the files for.
   * @return A List of Files, which is never null.
   */
  private List<File> safeListFiles(File dir) {
    File[] files = dir.listFiles();

    if (files == null) {
      return Collections.emptyList();
    }

    return asList(files);
  }

  private Collection<Class<U>> loadFromDirectory(File dir, Test<Class<U>> test, boolean recursive) throws IOException {
    Set<Class<U>> matches = new HashSet<Class<U>>();

    // Loop over the files
    Queue<File> files = new LinkedList<File>(safeListFiles(dir));
    while (!files.isEmpty()) {
      File file = files.poll();
      if (file.isDirectory() && recursive) {
        files.addAll(safeListFiles(file));
      } else if (file.isFile()) {
        // This file matches, test it
        Testable<Class<U>> testable = test.prepare(file);
        if (testable != null && testable.passes()) {
          matches.add(testable.result());
        }
      }
    }

    return matches;
  }

  private Collection<Class<U>> loadFromJar(File f, Test<Class<U>> test, boolean recursive, Iterable<String> locators, boolean embeddable)
    throws IOException {
    Set<Class<U>> matches = new HashSet<Class<U>>();

    JarFile jarFile;
    try {
      jarFile = new JarFile(f);
    } catch (IOException e) {
      throw new IOException("Error opening JAR file [" + f.getAbsolutePath() + "]");
    }

    Enumeration<JarEntry> en = jarFile.entries();
    while (en.hasMoreElements()) {
      JarEntry entry = en.nextElement();
      String name = entry.getName();

      // Verify against the locators
      for (String locator : locators) {
        int index = name.indexOf(locator + "/");
        boolean match = (!embeddable && index == 0) || (embeddable && index >= 0);
        if (!match) {
          continue;
        }

        match = recursive || name.indexOf('/', index + locator.length() + 1) == -1;
        if (!match) {
          continue;
        }

        Testable<Class<U>> testable = test.prepare(f, jarFile, entry);
        if (testable != null && testable.passes()) {
          matches.add(testable.result());
          break;
        }
      }
    }

    return matches;
  }

  /**
   * This is the testing interface that produces a testable object, given a file or JAR entry.
   */
  public static interface Test<T> {
    Testable<T> prepare(File file) throws IOException;

    Testable<T> prepare(File jar, JarFile jarFile, JarEntry jarEntry) throws IOException;
  }

  /**
   * This is the testing interface that is used to accept or reject resources.
   */
  public static interface Testable<T> {
    boolean passes();

    T result();
  }

  /**
   * Attempts to load the given file into a ClassReader.
   *
   * @param file The file to load.
   * @return The ClassReader and never null.
   * @throws IOException If the file doesn't point to a valid class.
   */
  public static ClassReader load(File file) throws IOException {
    try {
      return new ClassReader(new FileInputStream(file));
    } catch (IOException e) {
      throw new IOException("Error parsing class file at [" + file.getAbsolutePath() + "]");
    }
  }

  /**
   * Attempts to load the JarEntry into a ClassReader.
   *
   * @param jar      The JAR file that the entry is in.
   * @param jarFile  The JAR file used to get the InputStream to the entry.
   * @param jarEntry The JAR entry to load.
   * @return The ClassReader and never null.
   * @throws IOException If the JarEntry doesn't point to a valid class.
   */
  public static ClassReader load(File jar, JarFile jarFile, JarEntry jarEntry) throws IOException {
    try {
      return new ClassReader(jarFile.getInputStream(jarEntry));
    } catch (IOException e) {
      throw new IOException("Error parsing class file at [" + jar.getAbsolutePath() + "!/" +
        jarEntry.getName() + "]");
    }
  }

  /**
   * A Test that checks to see if each class is assignable to the provided class. Note that this test will match the
   * parent type itself if it is presented for matching.
   */
  public static class IsA<U> implements Test<Class<U>> {
    private final Class<U> parent;

    public IsA(Class<U> parent) {
      this.parent = parent;
    }

    public Testable<Class<U>> prepare(File file) throws IOException {
      if (!file.getName().endsWith(".class")) {
        return null;
      }
      return new IsATestable<U>(parent, load(file));
    }

    public Testable<Class<U>> prepare(File jar, JarFile jarFile, JarEntry jarEntry) throws IOException {
      if (!jarEntry.getName().endsWith(".class")) {
        return null;
      }
      return new IsATestable<U>(parent, load(jar, jarFile, jarEntry));
    }

    private static class IsATestable<U> implements Testable<Class<U>> {
      private final ClassReader classReader;
      private IsAClassVisitor<U> visitor;

      public IsATestable(Class<U> parent, ClassReader classReader) {
        this.visitor = new IsAClassVisitor<U>(parent);
        this.classReader = classReader;
      }

      public boolean passes() {
        classReader.accept(visitor, ClassReader.SKIP_CODE);
        return visitor.isPasses();
      }

      @SuppressWarnings("unchecked")
      public Class<U> result() {
        try {
          return (Class<U>) Thread.currentThread().getContextClassLoader().loadClass(classReader.getClassName().replace('/', '.'));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      private static class IsAClassVisitor<U> extends AbstractClassVisitor {
        private final Class<U> parent;
        private boolean passes;

        private IsAClassVisitor(Class<U> parent) {
          this.parent = parent;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
          String parentInternalName = parent.getName().replace('.', '/');
          passes = superName.equals(parentInternalName);
          if (passes) {
            return;
          }

          for (String anInterface : interfaces) {
            passes = anInterface.equals(parentInternalName);
            if (passes) {
              break;
            }
          }
          if (passes) {
            return;
          }

          // Walk the inheritance chain
          ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
          if (!superName.equals("java/lang/Object")) {
            try {
              InputStream is = classLoader.getResourceAsStream(superName.replace('.', '/') + ".class");
              ClassReader reader = new ClassReader(is);
              reader.accept(this, ClassReader.SKIP_CODE);
            } catch (IOException e) {
              // Smother and move on
            }
          }
          if (passes) {
            return;
          }

          // Walk the inheritance and implementation chains
          for (String anInterface : interfaces) {
            try {
              InputStream is = classLoader.getResourceAsStream(anInterface.replace('.', '/') + ".class");
              ClassReader reader = new ClassReader(is);
              reader.accept(this, ClassReader.SKIP_CODE);
            } catch (IOException e) {
              // Smother and move on
            }
            if (passes) {
              return;
            }
          }
        }

        public boolean isPasses() {
          return passes;
        }
      }
    }
  }

  /**
   * A Test that checks to see if each class is annotated with any of a number of annotations. If it is, then the test
   * returns true, otherwise false.
   */
  public static class AnnotatedWith<T extends Annotation, U> implements Test<Class<U>> {
    private final Class<T> annotation;

    public AnnotatedWith(Class<T> annotation) {
      this.annotation = annotation;
    }

    public Testable<Class<U>> prepare(File file) throws IOException {
      if (!file.getName().endsWith(".class")) {
        return null;
      }
      return new AnnotatedWithTestable<T, U>(annotation, load(file));
    }

    public Testable<Class<U>> prepare(File jar, JarFile jarFile, JarEntry jarEntry) throws IOException {
      if (!jarEntry.getName().endsWith(".class")) {
        return null;
      }
      return new AnnotatedWithTestable<T, U>(annotation, load(jar, jarFile, jarEntry));
    }

    private static class AnnotatedWithTestable<T extends Annotation, U> implements Testable<Class<U>> {
      private final ClassReader classReader;
      private AnnotatedWithClassVisitor<T> visitor;

      public AnnotatedWithTestable(Class<T> annotation, ClassReader classReader) {
        this.visitor = new AnnotatedWithClassVisitor<T>(annotation);
        this.classReader = classReader;
      }

      public boolean passes() {
        classReader.accept(visitor, ClassReader.SKIP_CODE);
        return visitor.isPasses();
      }

      @SuppressWarnings("unchecked")
      public Class<U> result() {
        try {
          return (Class<U>) Thread.currentThread().getContextClassLoader().loadClass(classReader.getClassName().replace('/', '.'));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      private static class AnnotatedWithClassVisitor<T extends Annotation> extends AbstractClassVisitor {
        private final Class<T> annotation;
        private boolean passes;

        private AnnotatedWithClassVisitor(Class<T> annotation) {
          this.annotation = annotation;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          desc = desc.replaceAll("^L|;$", "");
          desc = desc.replace('/', '.');

          passes |= desc.equals(annotation.getName());
          return null;
        }

        public boolean isPasses() {
          return passes;
        }
      }
    }
  }

  public static class AbstractClassVisitor implements ClassVisitor {
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      return null;
    }

    public void visitEnd() {
    }
  }
}