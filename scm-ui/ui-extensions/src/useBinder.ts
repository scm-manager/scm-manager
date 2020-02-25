import { createContext, useContext } from "react";
import defaultBinder from "./binder";

/**
 * The BinderContext should only be used to override the default binder for testing purposes.
 */
export const BinderContext = createContext(defaultBinder);

/**
 * Hook to get the binder from context.
 */
export const useBinder = () => {
  return useContext(BinderContext);
};

export default useBinder;
