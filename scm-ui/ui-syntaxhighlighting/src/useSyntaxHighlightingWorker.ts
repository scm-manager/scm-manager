// WebWorker which creates tokens for syntax highlighting
// @ts-ignore
import theme from "./syntax-highlighting.module.css";
import type { LoadThemeRequest } from "./types";

const worker = new Worker(
  // @ts-ignore TODO
  new URL("./SyntaxHighlighter.worker.ts", import.meta.url),
  {
    name: "SyntaxHighlighter",
    type: "module",
  }
);

worker.postMessage({ type: "theme", payload: theme } as LoadThemeRequest);

const useSyntaxHighlightingWorker = (): Worker => worker;
export default useSyntaxHighlightingWorker;
