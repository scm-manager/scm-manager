const { ESLint } = require("eslint");
const path = require("path");

describe("should lint files", () => {
  const eslint = new ESLint();
  const resource = path.join(__dirname, "__resources__");

  const lint = async file => {
    const results = await eslint.lintFiles([path.join(resource, file)]);

    const messages = results[0].messages;

    const warnings = messages.filter(m => m.severity === 1).map(m => m.ruleId);
    const errors = messages.filter(m => m.severity === 2).map(m => m.ruleId);
    return {
      errors,
      warnings
    };
  };

  const expectContains = (results, ...ids) => {
    for (const id of ids) {
      expect(results).toContain(id);
    }
  }

  it("should lint tsx files", async () => {
    const { errors, warnings } = await lint("TypescriptWithJsx.tsx");
    expectContains(errors, "no-console", "quotes", "semi");
    expectContains(warnings, "prettier/prettier");
  });

  it("should lint js files", async () => {
    const { errors, warnings } = await lint("Node.js");
    expectContains(errors, "no-var", "no-console", "quotes", "semi");
    expectContains(warnings, "prettier/prettier");
  });

  it("should lint ts files", async () => {
    const { errors, warnings } = await lint("Typescript.ts");
    expectContains(errors, "no-console", "quotes");
    expectContains(warnings, "prettier/prettier");
  });
});
