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

(function($){

  $.fn.extend({
    feeds: function( url, options ){
      return this.each( function(){
        new $.feeds(this, url, options);
      });
    }

  });

  $.feeds = function(field, url, options){

    var defaults = {
      maxItems: -1,
      paramName: "url",
      itemClass: "feeds",
      errorTitle: "Error",
      errorMsg: "could not load feeds",
      loadingImage: null
    };

    options = $.extend({},defaults, options);

    var $field = $(field);

    loadFeeds();

    function loadFeeds(){
      loadingView();
      $.ajax({
        type: "GET",
        url: url,
        dataType: "xml",
        success: renderFeeds,
        failure: renderErrorMsg
      });
    }

    function renderErrorMsg(){
      $field.append(
        $("<h3 />").text( options.errorTitle )
      ).append(
        $("<p />").text( options.errorMsg )
      );
    }

    function renderFeeds(xml){
      var ul = $('<ul />');
      $(xml).find('item').each(function(index){
        if ( options.maxItems <= 0 || index < options.maxItems ){
          var title = $(this).find('title').text();
          var link = $(this).find('link').text();
          ul.append(
            $('<li />').append(
              $('<a />').attr('target', '_blank').attr('href', link).text(title)
            )
          );
        }
      });
      clearLoadingView();
      $field.append(ul);
    }

    function loadingView(){
      $field.empty();
      if ( options.loadingImage != null ){
        $field.append(
          $("<div />").addClass("load").append(
            $("<img />").attr("src", options.loadingImage)
          )
        );
      }
    }

    function clearLoadingView(){
      $field.empty();
    }

  }


})(jQuery);
