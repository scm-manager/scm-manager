export type DefaultCollapsedFunction = (oldPath: string, newPath: string) => boolean;

export type DefaultCollapsed = boolean | DefaultCollapsedFunction;
