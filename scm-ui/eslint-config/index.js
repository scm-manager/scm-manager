module.exports = {
  extends: [
    "react-app",
    "plugin:prettier/recommended",
    "plugin:flowtype/recommended"
  ],
  rules: {
    semi: ["error", "always"],
    quotes: ["error", "double"],
    "jsx-a11y/href-no-hash": [0],
    "flowtype/no-types-missing-file-annotation": 2,
    "no-console": "error"
  },
  overrides: [
    {
      files: ["*.ts", "*.tsx"],
      parser: "@typescript-eslint/parser",
      extends: [
        "react-app",
        "plugin:prettier/recommended",
        "plugin:@typescript-eslint/recommended"
      ],
      rules: {
        semi: ["error", "always"],
        quotes: ["error", "double"],
        "jsx-a11y/href-no-hash": [0],
        "@typescript-eslint/explicit-function-return-type": "off",
        "no-console": "error"
      }
    }
  ]
};
