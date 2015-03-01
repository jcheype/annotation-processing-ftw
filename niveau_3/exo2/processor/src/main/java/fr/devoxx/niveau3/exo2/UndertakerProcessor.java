package fr.devoxx.niveau3.exo2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.common.annotations.VisibleForTesting;

import com.google.auto.service.AutoService;

/**
 * Un {@link Processor} qui écrit dans un fichier les prénoms des personnages du livre Game of Thrones morts en se
 * basant sur les informations disponibles dans l'annotation {@link Character}.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("fr.devoxx.niveau3.exo2.Character")
public class UndertakerProcessor extends AbstractProcessor {
  private final Set<String> deadCharacterFirstNames = new HashSet<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    collectDeadCharactersFirstNames(annotations, roundEnv);

    if (roundEnv.processingOver()) {
      generateListing();
    }
    return false;
  }

  /**
   * Collecte le nom de tous les classes annotées avec {@link Character} dont la propriété "dead"
   * vaut {@code true} du round courant dans la propriété {@link #deadCharacterFirstNames}.
   */
  private void collectDeadCharactersFirstNames(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return;
    }

    TypeElement characterAnnotationTypeElement = annotations.iterator().next();
    for (Element element : roundEnv.getElementsAnnotatedWith(characterAnnotationTypeElement)) {
      Character annotation = element.getAnnotation(Character.class);
      if (annotation.dead()) {
        deadCharacterFirstNames.add(element.getSimpleName().toString());
      }
    }
  }

  /**
   * Génère un fichier {@code dead_characters.txt} dans le package {@code fr.devoxx.niveau3.exo2} avec le nom des
   * personnages morts (un par ligne) en exploitant le contenu de {@link #deadCharacterFirstNames}.
   */
  private void generateListing() {
    try {
      FileObject listingResource = processingEnv.getFiler().createResource(
          StandardLocation.CLASS_OUTPUT,
          "fr.devoxx.niveau3.exo2",
          "dead_characters.txt"
      );

      try (BufferedWriter bufferedWriter = new BufferedWriter(listingResource.openWriter())) {
        for (String firstName : deadCharacterFirstNames) {
          bufferedWriter.write(String.valueOf(firstName));
          bufferedWriter.newLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @VisibleForTesting
  public Set<String> getDeadCharacterFirstNames() {
    return deadCharacterFirstNames;
  }
}
