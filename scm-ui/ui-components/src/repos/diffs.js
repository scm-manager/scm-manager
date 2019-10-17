// @flow
import type { BaseContext, File, Hunk } from "./DiffTypes";

export function getPath(file: File) {
  if (file.type === "delete") {
    return file.oldPath;
  }
  return file.newPath;
}

export function createHunkIdentifier(file: File, hunk: Hunk) {
  const path = getPath(file);
  return `${file.type}_${path}_${hunk.content}`;
}

export function createHunkIdentifierFromContext(ctx: BaseContext) {
  return createHunkIdentifier(ctx.file, ctx.hunk);
}
