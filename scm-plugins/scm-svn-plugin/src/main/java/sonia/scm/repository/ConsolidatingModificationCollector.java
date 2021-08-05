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

package sonia.scm.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

/**
 * This class can be used to "consolidate" modifications for a stream of commit based modifications.
 * Single modifications will be changed or ignored if subsequent modifications effect them. An "added"
 * for a file for example will be ignored, if the same file is marked as "removed" later on. Another
 * example would be a "modification" of a file, that was "added" beforehand, because in summary this
 * simply is still an "added" file.
 */
class ConsolidatingModificationCollector implements Collector<Modification, ConsolidatedModifications, Collection<Modification>> {

  static ConsolidatingModificationCollector consolidate() {
    return new ConsolidatingModificationCollector();
  }

  @Override
  public Supplier<ConsolidatedModifications> supplier() {
    return ConsolidatedModifications::new;
  }

  @Override
  public BiConsumer<ConsolidatedModifications, Modification> accumulator() {
    return (existingModifications, currentModification) -> {
      if (currentModification instanceof Copied) {
        existingModifications.added(new Added(((Copied) currentModification).getTargetPath()));
      } else if (currentModification instanceof Removed) {
        existingModifications.removed((Removed) currentModification);
      } else if (currentModification instanceof Renamed) {
        Renamed renameModification = (Renamed) currentModification;
        existingModifications.removed(new Removed(renameModification.getOldPath()));
        existingModifications.added(new Added(renameModification.getNewPath()));
      } else if (currentModification instanceof Added) {
        existingModifications.added((Added) currentModification);
      } else if (currentModification instanceof Modified) {
        existingModifications.modified((Modified) currentModification);
      } else {
        throw new IllegalStateException("cannot handle modification of unknown type " + currentModification.getClass());
      }
    };
  }

  @Override
  public BinaryOperator<ConsolidatedModifications> combiner() {
    return null; // Combiner not needed because we do not support Collector.Characteristics#CONCURRENT
  }

  @Override
  public Function<ConsolidatedModifications, Collection<Modification>> finisher() {
    return ConsolidatedModifications::getModifications;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return emptySet();
  }
}

class ConsolidatedModifications {
  private final Map<String, Modification> modifications = new HashMap<>();

  Set<String> getPaths() {
    return unmodifiableSet(modifications.keySet());
  }

  Collection<Modification> getModifications() {
    return unmodifiableCollection(modifications.values());
  }

  void added(Added added) {
    Modification earlierModification = modifications.get(added.getPath());
    if (earlierModification instanceof Removed) {
      modifications.put(added.getPath(), new Modified(added.getPath()));
    } else {
      modifications.put(added.getPath(), added);
    }
  }

  void modified(Modified modified) {
    Modification earlierModification = modifications.get(modified.getPath());
    if (!(earlierModification instanceof Added)) { // added should still be added
      modified.getEffectedPaths().forEach(path -> modifications.put(path, modified));
    }
  }

  void removed(Removed removed) {
    Modification earlierModification = modifications.get(removed.getPath());
    if (earlierModification instanceof Added) {
      modifications.remove(removed.getPath());
    } else {
      modifications.put(removed.getPath(), removed);
    }
  }
}
