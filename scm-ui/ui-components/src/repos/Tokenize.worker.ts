// @ts-ignore we have no types for react-diff-view
import { tokenize } from "react-diff-view";
import refractor from "./refractorAdapter";

// the WorkerGlobalScope is assigned to self
// see https://developer.mozilla.org/en-US/docs/Web/API/WorkerGlobalScope/self
declare const self: Worker;

self.addEventListener("message", ({ data: { id, payload } }) => {
  const { hunks, language } = payload;
  const options = {
    highlight: language !== "text",
    language: language,
    refractor
  };

  const doTokenization = (worker: Worker) => {
    try {
      const tokens = tokenize(hunks, options);
      const payload = {
        success: true,
        tokens: tokens
      };
      worker.postMessage({ id, payload });
    } catch (ex) {
      const payload = {
        success: false,
        reason: ex.message
      };
      worker.postMessage({ id, payload });
    }
  };

  const createTokenizer = (worker: Worker) => () => doTokenization(worker);

  if (options.highlight) {
    refractor.loadLanguage(language, createTokenizer(self));
  }
});
