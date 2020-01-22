/* eslint-disable no-restricted-globals */
// @ts-ignore we have no types for react-diff-view
import { tokenize } from "react-diff-view";
import refractor from "./refractorAdapter";

self.addEventListener("message", ({ data: { id, payload } }) => {
  const { hunks, language } = payload;
  const options = {
    highlight: language !== "text",
    language: language,
    refractor
  };
  try {
    const tokens = tokenize(hunks, options);
    const payload = {
      success: true,
      tokens: tokens
    };
    // @ts-ignore seems to use wrong typing
    self.postMessage({ id, payload });
  } catch (ex) {
    const payload = {
      success: false,
      reason: ex.message
    };
    // @ts-ignore seems to use wrong typing
    self.postMessage({ id, payload });
  }
});
