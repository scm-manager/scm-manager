/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
Ext.ns('Sonia.util');

// functions


Sonia.util.getServername = function(url){
  var i = url.indexOf('://');
  if ( i > 0 ){
    url = url.substring(i+3);
    i = url.indexOf(':');
    if ( i <= 0 ){
      i = url.indexof('/');
    }
    if ( i > 0 ){
      url = url.substring(0, i);
    }
  }
  return url;
}
  
Sonia.util.getContextPath = function(){
  var path = window.location.pathname;
  if ( path.indexOf('.html') > 0 ){
    var i = path.lastIndexOf('/');
    if ( i > 0 ){
      path = path.substring(0, i);
    }
  }
  return path;
}

Sonia.util.getName = function(path){
  var name = path;
  var index = path.lastIndexOf('/');
  if ( index > 0 ){
    name = path.substr(index +1);
  }
  return name;
}

Sonia.util.getExtension = function(path){
  var ext = null;
  var index = path.lastIndexOf('.');
  if ( index > 0 ){
    ext = path.substr(index + 1, path.length);
  }
  return ext;
}

Sonia.util.clone = function(obj) {
  var newObj = (this instanceof Array) ? [] : {};
  for (i in obj) {
    if (i == 'clone') continue;
    if (obj[i] && typeof obj[i] == "object") {
      newObj[i] = Sonia.util.clone(obj[i]);
    } else newObj[i] = obj[i]
  }
  return newObj;
};

Sonia.util.parseInt = function(string, defaultValue){
  var result = defaultValue;
  try {
    result = parseInt(string);
  } catch (e){
    if (debug){
      console.debug(e);
    }
  }
  if (isNaN(result)){
    result = defaultValue;
  }
  return result;
}

Sonia.util.getStringFromArray = function(array){
  var value = '';
  if ( Ext.isArray(array) ){
    for ( var i=0; i<array.length; i++ ){
      value += array[i];
      if ( (i+1)<array.length ){
        value += ', ';
      }
    }
  }

  return value;
}

Sonia.util.applySub = function(obj, name, p){
  if (name){
    obj = obj[name];
  }
  return Sonia.util.call(obj, p);
}

Sonia.util.apply = function(obj, p){
  var result = null;
  // use call instead of apply for compatiblity
  if (Ext.isFunction(obj)) {
    if (p){
      if (Ext.isArray(p)){
        result = obj(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9]);
      } else {
        result = obj(p);
      }
    } else {
      result = obj();
    }
  } else {
    if (p){
      if ( Ext.isArray(p) ){
        result = obj.fn.call(obj.scope, p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9]);
      } else {
        result = obj.fn.call(obj.scope, p);
      }
    } else {
      result = obj.fn.call(obj.scope);
    }
  }
  return result;
}

if (!Array.prototype.filter) {
  
  Array.prototype.filter = function(fn, scope){
    var results = [],
    i = 0,
    ln = array.length;

    for (; i < ln; i++) {
      if (fn.call(scope, array[i], i, array)) {
        results.push(array[i]);
      }
    }

    return results;

  }
  
}