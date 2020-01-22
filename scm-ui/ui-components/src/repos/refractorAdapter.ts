import lowlight from "lowlight";

// adapter to let lowlight look like refractor
// this is required because react-diff-view does only support refractor,
// but we want same highlighting as in the source code browser.

const refractorAdapter = {
  ...lowlight,
  highlight: (value: string, language: string) => {
    return lowlight.highlight(language, value).value;
  }
};

export default refractorAdapter;
