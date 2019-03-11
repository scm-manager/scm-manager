// @flow

import * as React from "react";

// We place the types here and not in @scm-manager/ui-types,
// because they represent not a real scm-manager related type.
// This types represents only the required types for the Diff related components,
// such as every other component does with its Props.

export type FileChangeType = "add" | "modify" | "delete" | "copy" | "rename";

export type File = {
  hunks: Hunk[],
  newEndingNewLine: boolean,
  newMode?: string,
  newPath: string,
  newRevision?: string,
  oldEndingNewLine: boolean,
  oldMode?: string,
  oldPath: string,
  oldRevision?: string,
  type: FileChangeType
};

export type Hunk = {
  changes: Change[],
  content: string
};

export type Change = {
  content: string,
  isNormal: boolean,
  newLineNumber: number,
  oldLineNumber: number,
  type: string
};

export type BaseContext = {
  hunk: Hunk,
  file: File
};

export type AnnotationFactoryContext = BaseContext;

export type FileAnnotationFactory = (file: File) => React.Node[];

// key = change id, value = react component
export type AnnotationFactory = (
  context: AnnotationFactoryContext
) => {
  [string]: any
};

export type DiffEventContext = BaseContext & {
  changeId: string,
  change: Change
};

export type DiffEventHandler = (context: DiffEventContext) => void;

export type FileControlFactory = (file: File, setCollapseState: (boolean) => void) => ?React.Node;

export type DiffObjectProps = {
  sideBySide: boolean,
  onClick?: DiffEventHandler,
  fileControlFactory?: FileControlFactory,
  fileAnnotationFactory?: FileAnnotationFactory,
  annotationFactory?: AnnotationFactory
};
