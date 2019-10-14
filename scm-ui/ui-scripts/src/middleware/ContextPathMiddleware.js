function crateContextPathMiddleware(contextPath) {
  return function(req, resp, next) {
    const url = req.url;
    if (url.indexOf(contextPath) === 0) {
      req.url = url.substr(contextPath.length);
    }
    next();
  }
}

module.exports = crateContextPathMiddleware;
