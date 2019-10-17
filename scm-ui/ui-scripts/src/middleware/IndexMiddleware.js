const mustache = require("mustache");
const fs = require("fs");
// disable escaping
mustache.escape = function(text) {
  return text;
};

function createIndexMiddleware(file, params) {
  const template = fs.readFileSync(file, { encoding: "UTF-8" });
  return function(req, resp, next) {
    if (req.url === "/index.html") {
      const content = mustache.render(template, params);
      resp.send(content);
    } else {
      next();
    }
  }
}

module.exports = createIndexMiddleware;
