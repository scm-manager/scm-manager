import i18n from "i18next";
import Backend from "i18next-fetch-backend";
import LanguageDetector from "i18next-browser-languagedetector";
import { reactI18nextModule } from "react-i18next";

const loadPath = process.env.PUBLIC_URL + "/locales/{{lng}}/{{ns}}.json";

// TODO load locales for moment

i18n
  .use(Backend)
  .use(LanguageDetector)
  .use(reactI18nextModule)
  .init({
    fallbackLng: "en",

    // have a common namespace used around the full app
    ns: ["commons"],
    defaultNS: "commons",

    debug: true,

    interpolation: {
      escapeValue: false // not needed for react!!
    },

    react: {
      wait: true
    },

    backend: {
      loadPath: loadPath,
      init: {
        credentials: "same-origin"
      }
    }
  });

export default i18n;
