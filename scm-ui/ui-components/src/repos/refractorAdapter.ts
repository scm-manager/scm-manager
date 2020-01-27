import lowlight from "lowlight/lib/core";

// adapter to let lowlight look like refractor
// this is required because react-diff-view does only support refractor,
// but we want same highlighting as in the source code browser.

const isLanguageRegistered = (lang: string) => {
  // @ts-ignore listLanguages seems unknown to type
  const registeredLanguages = lowlight.listLanguages();
  return !!registeredLanguages[lang];
};

const loadLanguage = (lang: string, callback: () => void) => {
  if (isLanguageRegistered(lang)) {
    callback();
  } else {
    import(
      /* webpackChunkName: "tokenizer-lowlight-[request]" */
      `highlight.js/lib/languages/${lang}`
    ).then(loadedLanguage => {
      lowlight.registerLanguage(lang, loadedLanguage.default);
      callback();
    });
  }
};

const refractorAdapter = {
  ...lowlight,
  isLanguageRegistered,
  loadLanguage,
  highlight: (value: string, language: string) => {
    return lowlight.highlight(language, value).value;
  }
};

export default refractorAdapter;
