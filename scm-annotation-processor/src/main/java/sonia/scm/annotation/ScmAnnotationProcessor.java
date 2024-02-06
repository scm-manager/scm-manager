/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.annotation;

import com.github.legman.Subscribe;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import org.kohsuke.MetaInfServices;
import org.mapstruct.Mapper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import sonia.scm.annotation.ClassSetElement.ClassWithAttributes;
import sonia.scm.config.ConfigValue;
import sonia.scm.plugin.PluginAnnotation;
import sonia.scm.plugin.Requires;

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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.util.ElementFilter.methodsIn;



@SupportedAnnotationTypes("*")
@MetaInfServices(Processor.class)
@SuppressWarnings({"Since16"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class ScmAnnotationProcessor extends AbstractProcessor {

  private static final String DESCRIPTOR_MODULE = "META-INF/scm/module.xml";
  private static final String DESCRIPTOR_PLUGIN = "META-INF/scm/plugin.xml";
  private static final String EL_MODULE = "module";
  private static final String EMPTY = "";
  private static final String PROPERTY_VALUE = "yes";
  private static final Set<String> SUBSCRIBE_ANNOTATIONS =
    ImmutableSet.of(Subscribe.class.getName());

  private static final Set<String> CONFIG_ANNOTATIONS = ImmutableSet.of(ConfigValue.class.getName());
  private static final Set<ClassAnnotation> CLASS_ANNOTATIONS =
    ImmutableSet.of(new ClassAnnotation("rest-resource", Path.class),
      new ClassAnnotation("rest-provider", Provider.class),
      new ClassAnnotation("mapper", Mapper.class),
      new ClassAnnotation("cli-command", CommandLine.Command.class));

  @Override
  public boolean process(Set<? extends TypeElement> annotations,
                         RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      Set<DescriptorElement> descriptorElements = Sets.newHashSet();
      Set<TypeElement> subscriberAnnotations = Sets.newHashSet();
      Set<TypeElement> configAnnotations = Sets.newHashSet();

      for (TypeElement e : annotations) {
        PluginAnnotation pa = e.getAnnotation(PluginAnnotation.class);

        if (pa != null) {
          scanForClassAnnotations(descriptorElements, roundEnv, e, pa.value());
        }

        if (SUBSCRIBE_ANNOTATIONS.contains(e.getQualifiedName().toString())) {
          subscriberAnnotations.add(e);
        }

        if (CONFIG_ANNOTATIONS.contains(e.getQualifiedName().toString())) {
          configAnnotations.add(e);
        }
      }

      for (ClassAnnotation ca : CLASS_ANNOTATIONS) {
        TypeElement annotation = findAnnotation(annotations,
          ca.annotationClass);

        if (annotation != null) {
          scanForClassAnnotations(descriptorElements, roundEnv, annotation,
            ca.elementName);
        }
      }

      for (TypeElement annotation : subscriberAnnotations) {
        scanForSubscriberAnnotations(descriptorElements, roundEnv, annotation);
      }
      for (TypeElement annotation : configAnnotations) {
        scanForConfigAnnotations(descriptorElements, roundEnv, annotation);
      }

      write(descriptorElements);
    }

    return false;
  }

  private void scanForConfigAnnotations(Set<DescriptorElement> descriptorElements, RoundEnvironment roundEnv, TypeElement annotation) {

    Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(annotation);

    for (Element element : elementsAnnotatedWith) {
      Map<String, String> attributesFromAnnotation = getAttributesFromAnnotation(element, annotation);
      String type = element.asType().toString();

      descriptorElements.add(new ConfigElement(attributesFromAnnotation, type));
    }

  }

  private TypeElement findAnnotation(Set<? extends TypeElement> annotations,
                                     Class<? extends Annotation> annotationClass) {
    TypeElement annotation = null;

    for (TypeElement typeElement : annotations) {
      // Replace $ with . to match subclasses
      if (typeElement.getQualifiedName().toString().equals(annotationClass.getName().replace("$", "."))) {
        annotation = typeElement;

        break;
      }
    }

    return annotation;
  }


  private File findDescriptor(Filer filer) throws IOException {
    FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, EMPTY,
      DESCRIPTOR_PLUGIN);
    File file = new File(f.toUri());

    if (!file.exists()) {
      f = filer.getResource(StandardLocation.CLASS_OUTPUT, EMPTY,
        DESCRIPTOR_MODULE);
      file = new File(f.toUri());
    }

    return file;
  }


  private Document parseDocument(File file) {
    Document doc = null;

    try {
      DocumentBuilder builder = createDocumentBuilder();

      if (file.exists()) {
        doc = builder.parse(file);
      } else {
        doc = builder.newDocument();
        doc.appendChild(doc.createElement(EL_MODULE));
      }
    } catch (ParserConfigurationException | SAXException | IOException
      | DOMException ex) {
      printException("could not parse document", ex);
    }

    return doc;
  }

  @SuppressWarnings("java:S2755") // we need to process https dtd, to avoid breaking intellij compilation on plugins
  private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "https");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    factory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    return factory.newDocumentBuilder();
  }


  private String prepareArrayElement(Object obj) {
    String v = obj.toString();

    if (v.startsWith("\"")) {
      v = v.substring(1);

      if (v.endsWith("")) {
        v = v.substring(0, v.length() - 1);
      }
    }

    return v;
  }


  private void printException(String msg, Throwable throwable) {
    processingEnv.getMessager().printMessage(Kind.ERROR, msg);

    String stack = Throwables.getStackTraceAsString(throwable);

    processingEnv.getMessager().printMessage(Kind.ERROR, stack);
  }


  private void scanForClassAnnotations(
    Set<DescriptorElement> descriptorElements, RoundEnvironment roundEnv,
    TypeElement annotation, String elementName) {

    Set<ClassWithAttributes> classes = Sets.newHashSet();

    for (Element e : roundEnv.getElementsAnnotatedWith(annotation)) {

      if (isClassOrInterface(e)) {
        TypeElement type = (TypeElement) e;

        String[] requires = null;
        Requires requiresAnnotation = type.getAnnotation(Requires.class);

        if (requiresAnnotation != null) {
          requires = requiresAnnotation.value();
        }

        String desc = processingEnv.getElementUtils().getDocComment(type);

        if (desc != null) {
          desc = desc.trim();
        }

        classes.add(
          new ClassWithAttributes(
            type.getQualifiedName().toString(), desc, requires, getAttributesFromAnnotation(e, annotation)
          )
        );
      }
    }

    descriptorElements.add(new ClassSetElement(elementName, classes));
  }


  private boolean isClassOrInterface(Element e) {
    return e.getKind().isClass() || e.getKind().isInterface();
  }


  private void scanForSubscriberAnnotations(
    Set<DescriptorElement> descriptorElements, RoundEnvironment roundEnv,
    TypeElement annotation) {
    for (Element el : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (el.getKind() == ElementKind.METHOD) {
        ExecutableElement ee = (ExecutableElement) el;
        List<? extends VariableElement> params = ee.getParameters();

        if ((params != null) && (params.size() == 1)) {
          VariableElement param = params.get(0);

          Element clazz = el.getEnclosingElement();
          String desc = processingEnv.getElementUtils().getDocComment(clazz);

          if (desc != null) {
            desc = desc.trim();
          }

          descriptorElements.add(
            new SubscriberElement(
              clazz.toString(),
              param.asType().toString(),
              desc
            )
          );
        }
      }
    }
  }


  private void write(Set<DescriptorElement> descriptorElements) {
    Filer filer = processingEnv.getFiler();

    try {
      File file = findDescriptor(filer);

      Document doc = parseDocument(file);

      if (doc != null) {
        org.w3c.dom.Element root = doc.getDocumentElement();

        for (DescriptorElement el : descriptorElements) {
          el.append(doc, root);
        }

        writeDocument(doc, file);
      }
    } catch (IOException ex) {
      printException("could not open plugin descriptor", ex);
    }
  }


  private void writeDocument(Document doc, File file) {
    try {
      file.getParentFile().mkdirs();

      Transformer transformer = createTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(file));
    } catch (IllegalArgumentException | TransformerException ex) {
      printException("could not write document", ex);
    }
  }

  @SuppressWarnings("java:S2755") // we need to process https dtd, to avoid breaking intellij compilation on plugins
  private Transformer createTransformer() throws TransformerConfigurationException {
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "https");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    Transformer transformer =  factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, PROPERTY_VALUE);

    return transformer;
  }

  private Map<String, String> getAttributesFromAnnotation(Element el,
                                                          TypeElement annotation) {
    Map<String, String> attributes = Maps.newHashMap();

    for (AnnotationMirror annotationMirror : el.getAnnotationMirrors()) {
      String qn = annotationMirror.getAnnotationType().asElement().toString();

      if (qn.equals(annotation.toString())) {
        for (Map.Entry<? extends ExecutableElement,
                  ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          attributes.put(entry.getKey().getSimpleName().toString(),
            getValue(entry.getValue()));
        }

        // add default values
        for (ExecutableElement meth : methodsIn(annotationMirror.getAnnotationType().asElement().getEnclosedElements())) {
          String attribute = meth.getSimpleName().toString();
          AnnotationValue defaultValue = meth.getDefaultValue();
          if (defaultValue != null && !attributes.containsKey(attribute)) {
            String value = getValue(defaultValue);
            if (value != null && !value.isEmpty()) {
              attributes.put(attribute, value);
            }
          }
        }
      }
    }

    return attributes;
  }


  private String getValue(AnnotationValue v) {
    String value;
    Object object = v.getValue();

    if (object instanceof Iterable) {
      Iterator<?> it = ((Iterable<?>) object).iterator();
      StringBuilder buffer = new StringBuilder();

      while (it.hasNext()) {
        buffer.append(prepareArrayElement(it.next()));

        if (it.hasNext()) {
          buffer.append(",");
        }
      }

      value = buffer.toString();
    } else {
      value = object.toString();
    }

    return value;
  }


  private static final class ClassAnnotation {

    public ClassAnnotation(String elementName,
                           Class<? extends Annotation> annotationClass) {

      this.elementName = elementName;
      this.annotationClass = annotationClass;
    }

    private final Class<? extends Annotation> annotationClass;
    private final String elementName;
  }
}
