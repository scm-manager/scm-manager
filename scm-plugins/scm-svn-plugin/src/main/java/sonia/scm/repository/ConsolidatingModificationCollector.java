/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
      modifications.put(modified.getPath(), modified);
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
