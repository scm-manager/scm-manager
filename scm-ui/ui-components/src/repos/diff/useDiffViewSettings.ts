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

import { ReactNode, useCallback, useMemo } from "react";
import classNames from "classnames";
import { ChangeData, DiffProps, EventMap, getChangeKey } from "react-diff-view";
import { Change, FileDiff, Hunk } from "@scm-manager/ui-types";
import { collectHunkAnnotations, findConflicts } from "./helpers";
import { DiffFileProps } from "./types";

type Options = Pick<DiffFileProps, "annotationFactory" | "onClick" | "hunkClass" | "markConflicts">;

type DiffViewSettings = Required<Pick<DiffProps, "widgets" | "generateLineClassName">> &
  Pick<DiffProps, "gutterEvents">;

/**
 * Since version 3, react-diff-view only accepts these settings for the whole diff and no longer per hunk.
 * This hook combines the settings of all hunks of the file.
 */
const useDiffViewSettings = (
  file: FileDiff,
  { annotationFactory, onClick, hunkClass, markConflicts }: Options,
): DiffViewSettings => {
  const hunksByChange = useMemo(() => {
    const result = new Map<Change, Hunk>();
    file.hunks?.forEach((hunk) => hunk.changes.forEach((change) => result.set(change, hunk)));
    return result;
  }, [file]);

  const conflicts = useMemo(
    () => new Set(markConflicts ? (file.hunks ?? []).flatMap(findConflicts) : []),
    [file, markConflicts],
  );

  const widgets = useMemo(() => {
    const result: Record<string, ReactNode> = {};
    file.hunks?.forEach((hunk) => {
      const annotations: Record<string, ReactNode> = collectHunkAnnotations(hunk, file, annotationFactory);
      // only use annotations for changes of the hunk, because that was the behaviour with annotations per hunk
      hunk.changes.forEach((change) => {
        const changeId = getChangeKey(change as ChangeData);
        if (annotations[changeId]) {
          result[changeId] = annotations[changeId];
        }
      });
    });
    return result;
  }, [annotationFactory, file]);

  const gutterEvents = useMemo<EventMap | undefined>(
    () =>
      onClick && {
        onClick: ({ change }) => {
          const hunk = change && hunksByChange.get(change);
          if (change && hunk) {
            onClick({
              changeId: getChangeKey(change),
              change,
              hunk,
              file,
            });
          }
        },
      },
    [file, hunksByChange, onClick],
  );

  const generateLineClassName = useCallback<DiffViewSettings["generateLineClassName"]>(
    ({ changes, defaultGenerate }) => {
      // in the split view, one side of a line could be empty
      const lineChanges = changes.filter((change) => !!change);
      const hunk = lineChanges.length > 0 ? hunksByChange.get(lineChanges[0]) : undefined;
      return classNames(defaultGenerate(), hunk && hunkClass?.(hunk), {
        "diff-line-conflict": lineChanges.some((change) => conflicts.has(change)),
      });
    },
    [conflicts, hunkClass, hunksByChange],
  );

  return { widgets, gutterEvents, generateLineClassName };
};

export default useDiffViewSettings;
