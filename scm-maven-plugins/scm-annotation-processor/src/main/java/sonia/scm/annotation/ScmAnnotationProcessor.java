/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.annotation;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.kohsuke.MetaInfServices;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import sonia.scm.annotation.ClassSetElement.ClassWithAttributes;
import sonia.scm.plugin.PluginAnnotation;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import java.lang.annotation.Annotation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Sebastian Sdorra
 */
@SupportedAnnotationTypes("*")
@MetaInfServices(Processor.class)
@SuppressWarnings({ "Since16", "Since15" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class ScmAnnotationProcessor extends AbstractProcessor
{

  /** Field description */
  private static final String DESCRIPTOR_MODULE = "META-INF/scm/module.xml";

  /** Field description */
  private static final String DESCRIPTOR_PLUGIN = "META-INF/scm/plugin.xml";

  /** Field description */
  private static final String EMPTY = "";

  /** Field description */
  private static final Set<String> SUBSCRIBE_ANNOTATIONS =
    ImmutableSet.of(Subscribe.class.getName());

  /** Field description */
  private static final Set<ClassAnnotation> CLASS_ANNOTATIONS =
    ImmutableSet.of(new ClassAnnotation("rest-resource", Path.class),
      new ClassAnnotation("rest-provider", Provider.class));

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param annotations
   * @param roundEnv
   *
   * @return
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations,
    RoundEnvironment roundEnv)
  {
    if (!roundEnv.processingOver())
    {
      Set<DescriptorElement> descriptorElements = Sets.newHashSet();
      Set<TypeElement> subscriberAnnotations = Sets.newHashSet();

      for (TypeElement e : annotations)
      {
        PluginAnnotation pa = e.getAnnotation(PluginAnnotation.class);

        if (pa != null)
        {
          scanForClassAnnotations(descriptorElements, roundEnv, e, pa.value());
        }

        if (SUBSCRIBE_ANNOTATIONS.contains(e.getQualifiedName().toString()))
        {
          subscriberAnnotations.add(e);
        }
      }

      for (ClassAnnotation ca : CLASS_ANNOTATIONS)
      {
        TypeElement annotation = findAnnotation(annotations,
                                   ca.annotationClass);

        if (annotation != null)
        {
          scanForClassAnnotations(descriptorElements, roundEnv, annotation,
            ca.elementName);
        }
      }

      for (TypeElement annotation : subscriberAnnotations)
      {
        scanForSubscriberAnnotations(descriptorElements, roundEnv, annotation);
      }

      write(descriptorElements);
    }

    return false;
  }

  /**
   * Method description
   *
   *
   * @param closeable
   */
  private void close(Closeable closeable)
  {
    if (closeable != null)
    {
      try
      {
        closeable.close();
      }
      catch (IOException ex)
      {
        printException("could not close closeable", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param annotations
   * @param annotationClass
   *
   * @return
   */
  private TypeElement findAnnotation(Set<? extends TypeElement> annotations,
    Class<? extends Annotation> annotationClass)
  {
    TypeElement annotation = null;

    for (TypeElement te : annotations)
    {
      if (te.getQualifiedName().toString().equals(annotationClass.getName()))
      {
        annotation = te;

        break;
      }
    }

    return annotation;
  }

  /**
   * Method description
   *
   *
   * @param filer
   *
   * @return
   *
   * @throws IOException
   */
  private File findDescriptor(Filer filer) throws IOException
  {
    FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, EMPTY,
                     DESCRIPTOR_PLUGIN);
    File file = new File(f.toUri());

    if (!file.exists())
    {
      f = filer.getResource(StandardLocation.CLASS_OUTPUT, EMPTY,
        DESCRIPTOR_MODULE);
      file = new File(f.toUri());
    }

    return file;
  }

  /**
   * Method description
   *
   *
   * @param f
   *
   * @param file
   *
   * @return
   */
  private Document parseDocument(File file)
  {
    Document doc = null;
    InputStream input = null;

    try
    {
      DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();

      if (file.exists())
      {
        input = new FileInputStream(file);
        doc = builder.parse(input);
      }
      else
      {
        doc = builder.newDocument();
        doc.appendChild(doc.createElement("module"));
      }
    }
    catch (ParserConfigurationException | SAXException | IOException
      | DOMException ex)
    {
      printException("could not parse document", ex);
    }
    finally
    {
      close(input);
    }

    return doc;
  }

  /**
   * Method description
   *
   *
   * @param msg
   * @param throwable
   */
  private void printException(String msg, Throwable throwable)
  {
    processingEnv.getMessager().printMessage(Kind.ERROR, msg);

    String stack = Throwables.getStackTraceAsString(throwable);

    processingEnv.getMessager().printMessage(Kind.ERROR, stack);
  }

  /**
   * Method description
   *
   *
   * @param descriptorElements
   * @param roundEnv
   * @param annotation
   * @param elementName
   * @param elements
   *
   * @return
   */
  private void scanForClassAnnotations(
    Set<DescriptorElement> descriptorElements, RoundEnvironment roundEnv,
    TypeElement annotation, String elementName)
  {
    Set<ClassWithAttributes> classes = Sets.newHashSet();

    for (Element e : roundEnv.getElementsAnnotatedWith(annotation))
    {
      if (e.getKind().isClass() || e.getKind().isInterface())
      {
        TypeElement type = (TypeElement) e;

        classes.add(new ClassWithAttributes(type.getQualifiedName().toString(),
          getAttributesFromAnnotation(e, annotation)));
      }
    }

    descriptorElements.add(new ClassSetElement(elementName, classes));
  }

  /**
   * Method description
   *
   *
   * @param descriptorElements
   * @param roundEnv
   * @param annotation
   */
  private void scanForSubscriberAnnotations(
    Set<DescriptorElement> descriptorElements, RoundEnvironment roundEnv,
    TypeElement annotation)
  {
    for (Element el : roundEnv.getElementsAnnotatedWith(annotation))
    {
      if (el.getKind() == ElementKind.METHOD)
      {
        ExecutableElement ee = (ExecutableElement) el;
        List<? extends VariableElement> params = ee.getParameters();

        if ((params != null) && (params.size() == 1))
        {
          VariableElement param = params.get(0);

          descriptorElements.add(
            new SubscriberElement(
              el.getEnclosingElement().toString(), param.asType().toString()));
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param descriptorElements
   */
  private void write(Set<DescriptorElement> descriptorElements)
  {
    Filer filer = processingEnv.getFiler();

    try
    {
      File file = findDescriptor(filer);

      Document doc = parseDocument(file);

      if (doc != null)
      {
        org.w3c.dom.Element root = doc.getDocumentElement();

        for (DescriptorElement el : descriptorElements)
        {
          el.append(doc, root);
        }

        writeDocument(doc, file);
      }
    }
    catch (IOException ex)
    {
      printException("could not open plugin descriptor", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param f
   * @param file
   */
  private void writeDocument(Document doc, File file)
  {
    Writer writer = null;

    try
    {
      file.getParentFile().mkdirs();
      writer = new FileWriter(file);

      Transformer transformer =
        TransformerFactory.newInstance().newTransformer();

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
    }
    catch (IOException | IllegalArgumentException | TransformerException ex)
    {
      printException("could not write document", ex);
    }
    finally
    {
      close(writer);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param el
   * @param annotation
   *
   * @return
   */
  private Map<String, String> getAttributesFromAnnotation(Element el,
    TypeElement annotation)
  {
    Map<String, String> attributes = Maps.newHashMap();

    for (AnnotationMirror am : el.getAnnotationMirrors())
    {
      String qn = am.getAnnotationType().asElement().toString();

      if (qn.equals(annotation.toString()))
      {
        for (Entry<? extends ExecutableElement,
          ? extends AnnotationValue> entry : am.getElementValues().entrySet())
        {
          attributes.put(entry.getKey().getSimpleName().toString(),
            entry.getValue().getValue().toString());
        }
      }
    }

    return attributes;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/03/18
   * @author         Enter your name here...
   */
  private static final class ClassAnnotation
  {

    /**
     * Constructs ...
     *
     *
     * @param elementName
     * @param annotationClass
     */
    public ClassAnnotation(String elementName,
      Class<? extends Annotation> annotationClass)
    {
      this.elementName = elementName;
      this.annotationClass = annotationClass;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Class<? extends Annotation> annotationClass;

    /** Field description */
    private final String elementName;
  }
}
