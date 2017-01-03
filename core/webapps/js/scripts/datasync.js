/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

var datasync_timeout = 2000;
var cookie_datasync_autorefresh = 'datasync_autorefresh';
var status_button;
var autorefresh_status = false;

sammy.bind(
  'datasync_load_data',
  function(event, params) {
    var core_basepath = params.active_core.attr('data-basepath');
    $.ajax({
      url: config.url + core_basepath + '/dsync?wt=json&command=list',
      dataType: 'json',
      beforeSend: function(xhr, settings) {},
      success: function(response, text_status, xhr) {
        var datasync_handlers = response.data.docs;
        var has_handler = false;
        for( handler in datasync_handlers ) {
          has_handler = true; break;
        }
        
        if( has_handler ) {
          params.success( datasync_handlers );
        } else {
          params.error();
        }        
      },
      error: function(xhr, text_status, error_thrown) {},
      complete: function(xhr, text_status) {}
    });
  }
);

sammy.bind
(
  'datasync_load_template',
  function( event, params )
  {
    if( app.datasync_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/datasync.html',
      function( template )
      {
        params.content_element
          .html( template );
             
        app.datasync_template = template;   
        params.callback();
      }
    );
  }
);

sammy.bind
(
  'datasync_load_file',
  function( event, params )
  {


    var core_basepath = params.active_core.attr('data-basepath');
    var endpoint = core_basepath + '/file?path=/sync/' + params.file + '&encode=utf-8';

    $( params.contain_element ).html( "" );

    //endpoint = "sync-news.xml";
      $.ajax(
        {
          url : endpoint,
          context : params.contain_element,
          beforeSend : function( xhr, settings )
          {
            if( !this.data( 'placeholder' ) )
            {
              this.data( 'placeholder', this.text() );
            }

            this
              .text( this.data( 'placeholder' ) );
          },
          success : function( response, text_status, xhr )
          {
            var content_type = xhr.getResponseHeader( 'Content-Type' ) || '';
            var highlight = null;
            var ccstr = null;

            if( 0 === content_type.indexOf( 'text/xml' ) ||  0 === xhr.responseText.indexOf( '<?xml' ) ||
                0 === content_type.indexOf( 'text/html' ) ||  0 === xhr.responseText.indexOf( '<!--' ) )
            {
              highlight = 'xml';
              ccstr = xhr.responseText;
            }
            else if( 0 === content_type.indexOf( 'text/javascript' ) ||  0 === content_type.indexOf( 'application/json' ))
            {
              highlight = 'javascript';
              ccstr = app.format_json(xhr.responseText);
            }

            //ccstr = $.prettyXml(ccstr);

            var code = $(
              '<pre class="syntax' + ( highlight ? ' language-' + highlight : '' )+ '"><code>' +
              ccstr.esc() +
              '</code></pre>'
            );
            $(this).html( code );

            if( highlight )
            {
              hljs.highlightBlock( code.get( 0 ) );
            }

            /*$( 'form textarea', this )
              .val( xhr.responseText );*/
          },
          error : function( xhr, text_status, error_thrown)
          {
            $(this ).text( 'No such file exists.' );
          },
          complete : function( xhr, text_status )
          {
          }
        }
      );
  }
);

// #/:core/datasync
sammy.get(
  new RegExp(app.core_regex_base + '\\/(datasync)$'),
  function(context) {
    delete app.datasync_template;
    var content_element = $( '#content' );
    var core_basepath = this.active_core.attr('data-basepath');
    var content_element = $('#content');
    var handler_url = core_basepath + '/dsync';

    sammy.trigger(
      'datasync_load_data', {
        active_core: this.active_core,
        success: function(datasync_handlers) {
          context.redirect(context.path + '/' + datasync_handlers[0].name);              
        },
        error: function() {
          sammy.trigger
          (
            'datasync_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var datasync_element = $('#datasync', content_element);
                var form_element = $('#form', datasync_element);
                var form_create_element = $('div.create', datasync_element);
                var handler_name = $('#add_name', form_create_element).val();
                var error_element = $('#error', datasync_element);
                var actions_element = $( '.actions', datasync_element );

                datasync_element.addClass('empty');

                //create
                $('#create', actions_element)
                  .die('click')
                  .live(
                    'click',
                    function(event) {
                      form_element.hide();
                      form_create_element.show();

                      return false;
                    }
                  ).trigger('click');
                
                  // form create
                  var form_create = $('form', form_create_element);
                  form_create
                    .ajaxForm({
                      url: config.url + handler_url,
                      data: {
                        wt: 'json',
                      },
                      dataType: 'json',
                      type: 'POST',
                      beforeSend: function(xhr, settings) {
                        $('button[type="submit"] span', form_create_element)
                          .addClass('loader');

                        error_element
                          .empty()
                          .hide();
                      },
                      beforeSubmit: function(array, form, options) {
                      },
                      success: function(response, text_status, xhr) {
                      },
                      error: function(xhr, text_status, error_thrown) {
                        var response = null;
                        try {
                          eval('response = ' + xhr.responseText + ';');
                        } catch (e) {}

                        error_element
                          .text(response.error.msg || 'Unknown Error (Exception w/o Message)')
                          .show();
                      },
                      complete: function(xhr, text_status) {
                          context.redirect(context.path+'/'+handler_name);
                      }
                    });
                }
              }
          );
        }
    });
});

// #/:core/datasync
sammy.get(
  new RegExp(app.core_regex_base + '\\/(datasync)\\/'),
  function(context) {
    var core_basepath = this.active_core.attr('data-basepath');
    var content_element = $('#content');
    var path_parts = this.path.match(/^(.+\/datasync\/)(.*)$/);
    var handler_name = path_parts[2];
    var handler_url = core_basepath + '/dsync';
    $('li.datasync', this.active_core)
      .addClass('active');

    sammy.trigger(
      'datasync_load_data', {
        active_core: this.active_core,
        error: function() {
          context.redirect( '#/' + context.params.splat[0] );
        },
        success: function(datasync_handlers) {
          sammy.trigger
          (
            'datasync_load_template',
            {
              content_element : content_element,
              callback : function() {

    var datasync_element = $('#datasync', content_element);

    var frame_element = $( '#frame', content_element );

    var form_element = $('#form', datasync_element);
    var form_create_element = $('div.create', datasync_element);
    var form_config_element = $('div.config', datasync_element);
    var error_element = $('#error', datasync_element);
    var debug_response_element = $('#debug_response', datasync_element);
    var actions_element = $( '.actions', datasync_element );
    var data_element = $( '#data', datasync_element );
    var source_data_element = $( '#source-data', data_element );

    var debug_mode = false;

    datasync_element
      .removeClass( 'empty' );


    // layout

    var ui_block = $( '#ui-block' );
    var actions_element = $( '.actions', datasync_element );
    var div_action = $( 'div.action', actions_element );

    ui_block
      .css( 'opacity', 0.7 )
      .width( datasync_element.width() + 10 )
      .height( datasync_element.height() );

    //var textarea_element = $( 'form #add_schema', div_action );
    //textarea_element.val(template_xml);


    $( 'button.action', actions_element )
      .die( 'click' )
      .live
      (
        'click',
        function( event )
        {
          var self = $( this );

          self
            .toggleClass( 'open' );

          $('#name', form_config_element).val(path_parts[2]);

          $( '.action.' + self.attr( 'id' ), actions_element )
            .trigger( 'open' );

          return false;
        }
      );

    div_action
      .die( 'close' )
      .live
      (
        'close',
        function( event )
        {
          div_action.hide();
          ui_block.hide();
        }
      )
      .die( 'open' )
      .live
      (
        'open',
        function( event )
        {
          var self = $( this );
          var rel = $( '#' + self.data( 'rel' ) );

          self
            .trigger( 'close' )
            .show()
            .css( 'left', rel.position().left );
          
          ui_block
            .show();
        }
      );

    $( 'form button.reset', actions_element )
      .die( 'click' )
      .live
      (
        'click',
        function( event )
        {
          $( this ).closest( 'div.action' )
            .trigger( 'close' );
        }
      );

    //remove
    $('#delete', actions_element)
      .die('click')
      .live(
        'click',
        function(event) {
          var ret = confirm( 'Do you really want to delete sync task "' + handler_name + '"?' );
          if( !ret )
          {
            return false;
          }

          $.ajax({
            url: config.url + handler_url + "?wt=json&command=remove&name=" + handler_name,
            dataType: 'json',
            type: 'POST',
            beforeSend: function(xhr, settings) {

            },
            success: function(response, text_status, xhr) {

            },
            error: function(xhr, text_status, error_thrown) {},
            complete: function(xhr, text_status) {
              sammy.trigger(
                'datasync_load_data', {
                  active_core: context.active_core,
                  success : function(datasync_handlers) {

                    context.redirect( path_parts[1] + datasync_handlers[0].name );

                    /*

                    var handlers_element = $('#navigation ul', form_element);
                    var handlers = [];

                    for (var i = 0; i < datasync_handlers.length; i++) {
                      handlers.push(
                        '<li><a href="' + path_parts[1] + datasync_handlers[i].name + '">' +
                        datasync_handlers[i].name +
                        '</a></li>'
                      );
                    }

                    $(handlers_element)
                      .html(handlers.join("\n"));

                    $('a[href="' + context.path + '"]', handlers_element).closest('li')
                      .addClass('current');

                    form_element.show();
                    form_create_element.hide();
                    */
                  },
                  error : function() {}
                }
              );
            }
          });
        }
      );

    //test
    $('#test', actions_element)
      .die('click')
      .live(
        'click',
        function(event) {
          var span_element = $('span', this);
          $.ajax({
            url: config.url + handler_url + "?wt=json&command=test&name=" + handler_name,
            dataType: 'json',
            type: 'POST',
            beforeSend: function(xhr, settings) {
              span_element.addClass( 'loader' );
            },
            success: function(response, text_status, xhr) {
              alert(response.data.msg);
            },
            error: function(xhr, text_status, error_thrown) {
              alert(xhr.responseText);
            },
            complete: function(xhr, text_status) {
              span_element.removeClass( 'loader' );
            }
          });
        }
      );

    // handler list
    var current_handler;
    var handlers_element = $('#navigation ul', form_element);
    var handlers = [];

    for (var i = 0; i < datasync_handlers.length; i++) {
      handlers.push(
        '<li><a href="' + path_parts[1] + datasync_handlers[i].name + '">' +
        datasync_handlers[i].name +
        '</a></li>'
      );
    }

    $(handlers_element)
      .html(handlers.join("\n"));

    $('a[href="' + context.path + '"]', handlers_element).closest('li')
      .addClass('current');

    $('form', form_element)
      .show();

    $('.block .toggle', datasync_element)
      .die('click')
      .live(
        'click',
        function(event) {
          $(this).parents('.block')
            .toggleClass('hidden');

          return false;
        }
      );
        
    // abort
    var abort_import_element = $('.abort-import', datasync_element);
    abort_import_element
      .off('click')
      .on(
        'click',
        function(event) {
          var span_element = $('span', this);

          $.ajax({
            url: config.url + handler_url + '?command=stop&wt=json&name=' + handler_name,
            dataType: 'json',
            type: 'POST',
            context: $(this),
            beforeSend: function(xhr, settings) {
              span_element
                .addClass('loader');
            },
            success: function(response, text_status, xhr) {
              span_element
                .data('original', span_element.text())
                .text(span_element.data('aborting'));

              this
                .removeClass('warn')
                .addClass('success');

              window.setTimeout(
                function() {
                  $('span', abort_import_element)
                    .removeClass('loader')
                    .text(span_element.data('original'))
                    .removeData('original');

                  abort_import_element
                    .removeClass('success')
                    .addClass('warn');
                },
                datasync_timeout * 2
              );

              datasync_fetch_status(handler_url);
            }
          });
          return false;
        }
      );

    // state
    status_button = $('form button.refresh-status', form_element);
    status_button
      .off('click')
      .on(
        'click',
        function(event) {
          datasync_fetch_status(handler_url);
          return false;
        }
      )
      .trigger('click');

    // form
    var form = $('form', form_element);
    form
      .ajaxForm({
        url: config.url + handler_url,
        data: {
          wt: 'json',
        },
        dataType: 'json',
        type: 'POST',
        beforeSend: function(xhr, settings) {
          $('button[type="submit"] span', form_element)
            .addClass('loader');

          error_element
            .empty()
            .hide();
        },
        beforeSubmit: function(array, form, options) {
          array.push({
            name: "command",
            value: "start"
          });
          array.push({
            name: "name",
            value: handler_name
          });

          $('input:checkbox', form).not(':checked')
            .each(function(i, input) {
              array.push({
                name: input.name,
                value: 'false'
              });
            });

          var custom_parameters = $('#custom_parameters', form).val();
          if (custom_parameters.length) {
            var params = custom_parameters.split('&');
            for (var i in params) {
              var tmp = params[i].split('=');
              array.push({
                name: tmp[0],
                value: tmp[1]
              });
            }
          }
        },
        success: function(response, text_status, xhr) {},
        error: function(xhr, text_status, error_thrown) {
          var response = null;
          try {
            eval('response = ' + xhr.responseText + ';');
          } catch (e) {}

          error_element
            .text(response.error.msg || 'Unknown Error (Exception w/o Message)')
            .show();
        },
        complete: function(xhr, text_status) {
          $('button[type="submit"] span', form_element)
            .removeClass('loader');

          var debug = $('input[name="debug"]:checked', form);
          if (0 !== debug.size()) {
            var code = $(
              '<pre class="syntax language-json"><code>' +
              app.format_json(xhr.responseText).esc() +
              '</code></pre>'
            );

            $('.content', debug_response_element).html(code);
            hljs.highlightBlock(code.get(0));
          }

          datasync_fetch_status(handler_url);
        }
      });

    // form create
    var form_create = $('form', form_create_element);
    form_create
      .ajaxForm({
        url: config.url + handler_url,
        data: {
          wt: 'json',
        },
        dataType: 'json',
        type: 'POST',
        beforeSend: function(xhr, settings) {
          $('button[type="submit"] span', form_create_element)
            .addClass('loader');

          error_element
            .empty()
            .hide();
        },
        beforeSubmit: function(array, form, options) {

        },
        success: function(response, text_status, xhr) {

        },
        error: function(xhr, text_status, error_thrown) {
          var response = null;
          try {
            eval('response = ' + xhr.responseText + ';');
          } catch (e) {}

          error_element
            .text(response.error.msg || 'Unknown Error (Exception w/o Message)')
            .show();
        },
        complete: function(xhr, text_status) {
          $('button[type="submit"] span', form_create_element)
            .removeClass('loader');  
                    
          sammy.trigger(
            'datasync_load_data', {
              active_core: context.active_core,
              success: function(datasync_handlers) {

                div_action.hide();
                ui_block.hide();
                context.redirect( path_parts[1] + $("#add_name", form_create_element).val() );

/*
                var handlers_element = $('#navigation ul', form_element);
                var handlers = [];

                for (var i = 0; i < datasync_handlers.length; i++) {
                  handlers.push(
                    '<li><a href="' + path_parts[1] + datasync_handlers[i].name + '">' +
                    datasync_handlers[i].name +
                    '</a></li>'
                  );
                }

                $(handlers_element)
                  .html(handlers.join("\n"));

                var currentPath = path_parts[1] + $("#add_name", form_create_element).val();
                $('a[href="' + currentPath + '"]', handlers_element).closest('li')
                  .addClass('current');

                div_action.hide();
                ui_block.hide();
                */
              },
              error : function() {}
            }
          );
        }
      });

    // form config
    var form_config = $('form', form_config_element);
    form_config
      .ajaxForm({
        url: config.url + handler_url,
        data: {
          wt: 'json',
        },
        dataType: 'json',
        type: 'POST',
        beforeSend: function(xhr, settings) {
          $('button[type="submit"] span', form_config_element)
            .addClass('loader');

          error_element
            .empty()
            .hide();
        },
        beforeSubmit: function(array, form, options) {

        },
        success: function(response, text_status, xhr) {

        },
        error: function(xhr, text_status, error_thrown) {
          var response = null;
          try {
            eval('response = ' + xhr.responseText + ';');
          } catch (e) {}

          error_element
            .text(response.error.msg || 'Unknown Error (Exception w/o Message)')
            .show();
        },
        complete: function(xhr, text_status) {
          $('button[type="submit"] span', form_config_element)
            .removeClass('loader');  
                      
          sammy.trigger(
            'datasync_load_data', {
              active_core: context.active_core,
              success: function(datasync_handlers) {
                div_action.hide();
                ui_block.hide();
                //context.redirect( path_parts[1] + $("#name", form_config_element).val() );
                sammy.trigger
                (
                  'datasync_load_file',
                  {
                    active_core: context.active_core,
                    selected_handler: handler_name,
                    file: handler_name + ".xml",
                    frame_element: frame_element,
                    contain_element: $('.content .response', frame_element)
                  }
                );   

                sammy.trigger
                (
                  'datasync_load_file',
                  {
                    active_core: context.active_core,
                    selected_handler: handler_name,
                    file: handler_name + "_params.json",
                    frame_element: frame_element,
                    contain_element: $('#raw_output_container', frame_element)
                  }
                ); 
              },
              error : function() {}
            }
          );
        }
      });

    //auto-refresh
    $('#auto-refresh-status a', form_element)
      .off('click')
      .on(
        'click',
        function(event) {
          $.cookie(cookie_datasync_autorefresh, $.cookie(cookie_datasync_autorefresh) ? null : true);
          $(this).trigger('state');

          datasync_fetch_status(handler_url);

          return false;
        }
      )
      .off('state')
      .on(
        'state',
        function(event) {
          autorefresh_status = !!$.cookie(cookie_datasync_autorefresh);

          $.cookie(cookie_datasync_autorefresh) ? $(this).addClass('on') : $(this).removeClass('on');
        }
      )
      .trigger('state');

      sammy.trigger
      (
        'datasync_load_file',
        {
          active_core: context.active_core,
          selected_handler: handler_name,
          file: handler_name + ".xml",
          frame_element: frame_element,
          contain_element: $('.content .response', frame_element)
        }
      ); 

      sammy.trigger
      (
        'datasync_load_file',
        {
          active_core: context.active_core,
          selected_handler: handler_name,
          file: handler_name + "_params.json",
          frame_element: frame_element,
          contain_element: $('#raw_output_container', frame_element)
        }
      );      
    
        }//callback
      }); //trigger instances_load_template
    }//success
  }); //trigger datasync_load_data
}); //sammy.get

function datasync_fetch_status(handler_url, clear_timeout) {
  if (clear_timeout) {
    app.clear_timeout();
  }
  var content_element = $( '#content' );
  var datasync_element = $('#datasync', content_element);
  var form_element = $('#form', datasync_element);
  var form_create_element = $('.create', datasync_element);
  var handler_name = $('#add_name', form_create_element).val();
  var error_element = $('#error', datasync_element);

  $.ajax({
    url: config.url + handler_url + '?command=status&wt=json',
    dataType: 'json',
    beforeSend: function(xhr, settings) {
      $('span', status_button)
        .addClass('loader');
    },
    success: function(response, text_status, xhr) {
      var state_element = $('#current_state', content_element);

      var statuses = response.data.docs;
      var status;
      for (var key in statuses) {
        status = statuses[key];
        if (status.name == handler_name)
          break;
      }

      if (status == undefined) return;

      status = status.status;
      var messages = status.messages;
      var messages_count = 0;
      for (var key in messages) {
        messages_count++;
      }

      function datasync_compute_details(status, details_element, elapsed_seconds) {
        details_element
          .show();

        // --

        var document_config = {
          'Requests': 'Total Requests made to DataSource',
          'Fetched': 'Total Rows Fetched',
          'Skipped': 'Total Documents Skipped',
          'Processed': 'Total Documents Processed',
          'DataSource': 'Total Requests made to DataSource'
        };

        var document_details = [];
        for (var key in document_config) {
          var value = parseInt(status.messages[document_config[key]], 10);

          var detail = '<abbr title="' + document_config[key].esc() + '">' + key.esc() + '</abbr>: ' + app.format_number(value).esc();
          if (elapsed_seconds && 'skipped' !== key.toLowerCase()) {
            detail += ' <span>(' + app.format_number(Math.round(value / elapsed_seconds)).esc() + '/s)</span>'
          }

          document_details.push(detail);
        };

        $('.docs', details_element)
          .html(document_details.join(', '));

        // --

        var dates_config = {
          'Started': 'Full Dump Started',
          'Aborted': 'Aborted',
          'Rolledback': 'Rolledback'
        };

        var dates_details = [];
        for (var key in dates_config) {
          var value = status.messages[dates_config[key]];

          if (value) {
            var detail = '<abbr title="' + dates_config[key].esc() + '">' + key.esc() + '</abbr>: ' + '<abbr class="time">' + value.esc() + '</abbr>';
            dates_details.push(detail);
          }
        };

        var dates_element = $('.dates', details_element);

        dates_element
          .html(dates_details.join(', '));

        $('.time', dates_element)
          .removeData('timeago')
          .timeago();
      };

      var get_time_taken = function get_default_time_taken() {
        var time_taken_text = status.messages['Time taken'];
        return app.convert_duration_to_seconds(time_taken_text);
      };

      var get_default_info_text = function default_info_text() {
        var info_text = status.messages[''] || '';

        // format numbers included in status nicely
        info_text = info_text.replace(
          /\d{4,}/g,
          function(match, position, string) {
            return app.format_number(parseInt(match, 10));
          }
        );

        var time_taken_text = app.convert_seconds_to_readable_time(get_time_taken());
        if (time_taken_text) {
          info_text += ' (Duration: ' + time_taken_text.esc() + ')';
        }

        return info_text;
      };

      var show_info = function show_info(info_text, elapsed_seconds) {
        $('.info strong', state_element)
          .text(info_text || get_default_info_text());

        $('.info .details', state_element)
          .hide();
      };

      var show_full_info = function show_full_info(info_text, elapsed_seconds) {
        show_info(info_text, elapsed_seconds);

        datasync_compute_details
          (
            status,
            $('.info .details', state_element),
            elapsed_seconds || get_time_taken()
          );
      };

      state_element
        .removeAttr('class');

      var current_time = new Date();
      $('.last_update abbr', state_element)
        .text(current_time.toTimeString().split(' ').shift())
        .attr('title', current_time.toUTCString());

      $('.info', state_element)
        .removeClass('loader');

      if ('busy' === status.status) {
        state_element
          .addClass('indexing');

        if (autorefresh_status) {
          $('.info', state_element)
            .addClass('loader');
        }

        var time_elapsed_text = status.messages['Time Elapsed'];
        var elapsed_seconds = app.convert_duration_to_seconds(time_elapsed_text);
        time_elapsed_text = app.convert_seconds_to_readable_time(elapsed_seconds);

        var info_text = time_elapsed_text ? 'Indexing since ' + time_elapsed_text : 'Indexing ...';

        show_full_info(info_text, elapsed_seconds);
      }
      /*else if (rollback_time) {
                         state_element
                           .addClass('failure');

                         show_full_info();
                       } else if (abort_time) {
                         state_element
                           .addClass('aborted');

                         show_full_info('Aborting current Import ...');
                       } */
      else if ('idle' === status.status && 0 !== messages_count) {
        state_element
          .addClass('success');

        show_full_info();
      } else {
        state_element
          .addClass('idle');

        show_info('No information available (idle)');
      }


      if (!app.timeout && autorefresh_status) {
        app.timeout = window.setTimeout(
          function() {
            datasync_fetch_status(handler_url, true)
          },
          datasync_timeout
        );
      }
    },
    error: function(xhr, text_status, error_thrown) {
      console.debug(arguments);

      reload_config_element
        .addClass('error');
    },
    complete: function(xhr, text_status) {
      $('span', status_button)
        .removeClass('loader')
        .addClass('success');

      window.setTimeout(
        function() {
          $('span', status_button)
            .removeClass('success');
        },
        datasync_timeout / 2
      );
    }
  });
}