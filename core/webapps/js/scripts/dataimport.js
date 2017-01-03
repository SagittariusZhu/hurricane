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

var dataimport_timeout = 2000;
var cookie_dataimport_autorefresh = 'dataimport_autorefresh';
var status_button;
var autorefresh_status = false;

sammy.bind(
  'dataimport_queryhandler_load',
  function(event, params) {
    var core_basepath = params.active_core.attr('data-basepath');
    $.ajax({
      url: config.url + core_basepath + '/dih?wt=json&command=list',
      dataType: 'json',
      beforeSend: function(xhr, settings) {},
      success: function(response, text_status, xhr) {
        var dataimport_handlers = response.data.docs;
        var has_handler = false;
        for( handler in dataimport_handlers ) {
          has_handler = true; break;
        }
        
        if( has_handler ) {
          params.success( dataimport_handlers );
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
  'dataimport_load_template',
  function( event, params )
  {
    if( app.dataimport_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/dataimport.html',
      function( template )
      {
        params.content_element
          .html( template );
             
        app.dataimport_template = template;   
        params.callback();
      }
    );
  }
);

sammy.bind
(
  'dataimport_load_source_table',
  function( event, params )
  {
    var levels = '<div class="selector-holder"><div class="selector">' + "\n"
               + '<a class="trigger"><span><em>unWatch</em></span></a>' + "\n"
               + '<ul>' + "\n";

    for( var key in params.levels )
    {
      var level = params.levels[key];
      levels += '<li><a href="#" data-level="' + level + '">' + level + '</a></li>' + "\n";
    }

    //levels += '<li class="unset"><a href="#" data-level="unset">UNSET</a></li>' + "\n"
    levels += '</ul>' + "\n"
           + '<a class="close"><span>[x]</span></a>' + "\n"
           + '</div></div>';

    var table_content = ['<ul>'];

    for( var idx in params.source_data.tables )
    {
      var table = params.source_data.tables[idx];
      var table_name = table.name
      if( !table_name )
      {
        table_name = '<em>(empty)</em>';
      }
      item_content = '';
      item_content += '<li class="table"><dl class="clearfix">' + '\n'
                      + '<dt><a href="#" class="trigger null" title="' + table_name + '">' + '\n'
                      +   '<span class="name">' + table_name + ':</span></a></dt>' + '\n'
                      + '<dd>' + table.desc + '</dd></dl>' + '\n';
      item_content += levels;
      item_content += '</li>';
      table_content.push( item_content );
    }

    var table_element = $( '.content', params.source_data_element );
    table_element
      .html( table_content.join( "\n" ) );

    var self = table_element;
    self
      .die( 'clear' )
      .live
      (
        'clear',
        function( event )
        {
          $( '.open', self )
            .removeClass( 'open' );
        }
      );

    $( '.trigger', self )
      .die( 'click' )
      .live
      (
        'click',
        function( event )
        {
          self.trigger( 'clear' );

          $( '.selector-holder', $( this ).parents( 'li' ).first() ).first()
            .trigger( 'toggle' );

          return false;
        }
      );

    $( '.selector .close', self )
      .die( 'click' )
      .live
      (
        'click',
        function( event )
        {
          self.trigger( 'clear' );
          return false;
        }
      );
      
    $( '.selector-holder', self )
      .die( 'toggle')
      .live
      (
        'toggle',
        function( event )
        {
          var row = $( this ).closest( 'li' );

          $( 'a:first', row )
            .toggleClass( 'open' );

          $( '.selector-holder:first', row )
            .toggleClass( 'open' );
        }
      );

    $( '.selector ul a', self )
      .die( 'click' )
      .live
      (
        'click',
        function( event )
        {
          var element = $( this );

          var selector = $( '.selector-holder', $( this ).parents( 'li.table' ).first() ).first();

          var trigger = $( 'a.trigger', selector );

          var level = element.data( 'level' );

          trigger
            .text( level );

          self.trigger( 'clear' );

          return false;
        }
      );      
  }
);

// #/:core/dataimport
sammy.get(
  new RegExp(app.core_regex_base + '\\/(dataimport)$'),
  function(context) {
    delete app.dataimport_template;
    var content_element = $( '#content' );
    var core_basepath = this.active_core.attr('data-basepath');
    var content_element = $('#content');
    var handler_url = core_basepath + '/dih';

    sammy.trigger(
      'dataimport_queryhandler_load', {
        active_core: this.active_core,
        success: function(dataimport_handlers) {
          context.redirect(context.path + '/' + dataimport_handlers[0].name);              
        },
        error: function() {
          sammy.trigger
          (
            'dataimport_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var dataimport_element = $('#dataimport', content_element);
                var form_element = $('#form', dataimport_element);
                var form_create_element = $('div.create', dataimport_element);
                var handler_name = $('#add_name', form_create_element).val();
                var error_element = $('#error', dataimport_element);
                var actions_element = $( '.actions', dataimport_element );

                dataimport_element.addClass('empty');

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
                          .text(response.data.failure || 'Unknown Error (Exception w/o Message)')
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

// #/:core/dataimport
sammy.get(
  new RegExp(app.core_regex_base + '\\/(dataimport)\\/'),
  function(context) {
    var core_basepath = this.active_core.attr('data-basepath');
    var content_element = $('#content');
    var path_parts = this.path.match(/^(.+\/dataimport\/)(.*)$/);
    var handler_name = path_parts[2];
    var handler_url = core_basepath + '/dih';
    $('li.dataimport', this.active_core)
      .addClass('active');

    sammy.trigger(
      'dataimport_queryhandler_load', {
        active_core: this.active_core,
        error: function() {
          context.redirect( '#/' + context.params.splat[0] );
        },
        success: function(dataimport_handlers) {
          sammy.trigger
          (
            'dataimport_load_template',
            {
              content_element : content_element,
              callback : function() {

    var dataimport_element = $('#dataimport', content_element);
    var form_element = $('#form', dataimport_element);
    var form_create_element = $('div.create', dataimport_element);
    var form_config_element = $('div.config', dataimport_element);
    var config_element = $('#config', dataimport_element);
    var error_element = $('#error', dataimport_element);
    var debug_response_element = $('#debug_response', dataimport_element);
    var actions_element = $( '.actions', dataimport_element );
    var data_element = $( '#data', dataimport_element );
    var source_data_element = $( '#source-data', data_element );

    var debug_mode = false;

    dataimport_element
      .removeClass( 'empty' );

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
      );

    //reset
    $('.reset', form_create_element)
      .die('click')
      .live(
        'click',
        function(event) {
          form_element.show();
          form_create_element.hide();

          return false;
        }
      );      

    //config
    $('#config', actions_element)
      .die('click')
      .live(
        'click',
        function(event) {
          form_config_element.show();

          return false;
        }
      );

    //reset
    $('.reset', form_config_element)
      .die('click')
      .live(
        'click',
        function(event) {
          form_config_element.hide();

          return false;
        }
      );      

    //remove
    $('#delete', actions_element)
      .die('click')
      .live(
        'click',
        function(event) {

          var ret = confirm( 'Do you really want to delete dih "' + handler_name + '"?' );
          if( !ret )
          {
            return false;
          }

          var span_element = $('span', this);
          $.ajax({
            url: config.url + handler_url + "?wt=json&command=remove&name=" + handler_name,
            dataType: 'json',
            type: 'POST',
            beforeSend: function(xhr, settings) {
              span_element
                .addClass('loader');
            },
            success: function(response, text_status, xhr) {

            },
            error: function(xhr, text_status, error_thrown) {},
            complete: function(xhr, text_status) {
              sammy.trigger(
                'dataimport_queryhandler_load', {
                  active_core: context.active_core,
                  success : function(dataimport_handlers) {
                    var handlers_element = $('#navigation ul', form_element);
                    var handlers = [];

                    for (var i = 0; i < dataimport_handlers.length; i++) {
                      handlers.push(
                        '<li><a href="' + path_parts[1] + dataimport_handlers[i].name + '">' +
                        dataimport_handlers[i].name +
                        '</a></li>'
                      );
                    }

                    $(handlers_element)
                      .html(handlers.join("\n"));

                    $('a[href="' + path_parts[1] + dataimport_handlers[0].name + '"]', handlers_element).closest('li')
                      .addClass('current');

                    form_element.show();
                    form_create_element.hide();
                    span_element.removeClass('loader');
                    context.redirect(path_parts[1] + dataimport_handlers[0].name);
                  },
                  error : function() {}
                }
              );
            }
          });
        }
      );



    // handler list
    var current_handler;
    var handlers_element = $('#navigation ul', form_element);
    var handlers = [];

    for (var i = 0; i < dataimport_handlers.length; i++) {
      var deltaStatus = '';
      if ('deltaStatus' in dataimport_handlers[i]) {
        deltaStatus = ' [' + dataimport_handlers[i].deltaStatus + ']';
      }
      handlers.push(
        '<li><a href="' + path_parts[1] + dataimport_handlers[i].name + '" refid="' + dataimport_handlers[i].name + '">' +
        dataimport_handlers[i].name + deltaStatus + 
        '</a></li>'
      );
    }

    $(handlers_element)
      .html(handlers.join("\n"));

    $('a[href="' + context.path + '"]', handlers_element).closest('li')
      .addClass('current');

    $('form', form_element)
      .show();

    $('.block .toggle', dataimport_element)
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
    var abort_import_element = $('.abort-import', dataimport_element);
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
                dataimport_timeout * 2
              );

              dataimport_fetch_status(handler_url);
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
          dataimport_fetch_status(handler_url);
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

          var ret = confirm( 'Do you really want to execute dih "' + handler_name + '"?' );
          if( !ret )
          {
            return false;
          }            
        },
        success: function(response, text_status, xhr) {},
        error: function(xhr, text_status, error_thrown) {
          var response = null;
          try {
            eval('response = ' + xhr.responseText + ';');
          } catch (e) {}

          error_element
            .text(response.data.failure || 'Unknown Error (Exception w/o Message)')
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

          dataimport_fetch_status(handler_url);
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
            .text(response.data.failure || 'Unknown Error (Exception w/o Message)')
            .show();
        },
        complete: function(xhr, text_status) {
          $('button[type="submit"] span', form_create_element)
            .removeClass('loader');
          sammy.trigger(
            'dataimport_queryhandler_load', {
              active_core: context.active_core,
              success : function(dataimport_handlers) {
                var handlers_element = $('#navigation ul', form_element);
                var handlers = [];

                for (var i = 0; i < dataimport_handlers.length; i++) {
                  handlers.push(
                    '<li><a href="' + path_parts[1] + dataimport_handlers[i].name + '">' +
                    dataimport_handlers[i].name +
                    '</a></li>'
                  );
                }

                $(handlers_element)
                  .html(handlers.join("\n"));

                var handler_name = $('#add_name', form_create_element).val();

                $('a[href="' + path_parts[1] + handler_name + '"]', handlers_element).closest('li')
                  .addClass('current');

                form_element.show();
                form_create_element.hide();
                context.redirect(path_parts[1] + handler_name);
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
            .text(response.data.failure || 'Unknown Error (Exception w/o Message)')
            .show();
        },
        complete: function(xhr, text_status) {
          $('button[type="submit"] span', form_config_element)
            .removeClass('loader');          
          sammy.trigger(
            'dataimport_queryhandler_load', {
              active_core: context.active_core,
              success : function(dataimport_handlers) {
                var handlers_element = $('#navigation ul', form_element);
                var handlers = [];

                for (var i = 0; i < dataimport_handlers.length; i++) {
                  handlers.push(
                    '<li><a href="' + path_parts[1] + dataimport_handlers[i].name + '">' +
                    dataimport_handlers[i].name +
                    '</a></li>'
                  );
                }

                $(handlers_element)
                  .html(handlers.join("\n"));

                $('a[href="' + context.path + '"]', handlers_element).closest('li')
                  .addClass('current');

                form_element.show();
                form_config_element.hide();
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
          $.cookie(cookie_dataimport_autorefresh, $.cookie(cookie_dataimport_autorefresh) ? null : true);
          $(this).trigger('state');

          dataimport_fetch_status(handler_url);

          return false;
        }
      )
      .off('state')
      .on(
        'state',
        function(event) {
          autorefresh_status = !!$.cookie(cookie_dataimport_autorefresh);

          $.cookie(cookie_dataimport_autorefresh) ? $(this).addClass('on') : $(this).removeClass('on');
        }
      )
      .trigger('state');

      // tables-data
      var levels = ["left join", "right join", "Unwatch"];
      var source_data = {
          name:"aaa", 
          dbType:"mysql", 
          connectURL:"jdbc:mysql:localhost@aaa/pass:test", 
          mainTable:"test with PK(uid)", 
          tables:[
            {name:"tableA", desc:"this is A table."}, 
            {name:"tableB", desc:"this is B table."}
          ]
      };

      sammy.trigger
      (
        'dataimport_load_source_table',
        {
          source_data : source_data,
          levels : levels,
          source_data_element : source_data_element
        }
      );      
    
        }//callback
      }); //trigger instances_load_template
    }//success
  }); //trigger dataimport_queryhandler_load
}); //sammy.get

function dataimport_fetch_status(handler_url, clear_timeout) {
  if (clear_timeout) {
    app.clear_timeout();
  }
  var content_element = $( '#content' );
  var dataimport_element = $('#dataimport', content_element);
  var navigation_element = $('#navigation', dataimport_element);
  var handler_element = $('li.current a', navigation_element);
  var handler_name = handler_element.attr('refid');
  var error_element = $('#error', dataimport_element);
  var form_config_element = $('div.config', dataimport_element);

  $.ajax({
    url: config.url + handler_url + '?command=status&wt=json&name=' + handler_name,
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
      status = status.status;
      var messages = status.messages;
      var messages_count = 0;
      for (var key in messages) {
        messages_count++;
      }

      function dataimport_compute_details(status, details_element, elapsed_seconds) {
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

        dataimport_compute_details
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

      if ('deltaStatus' in status) {
        state_element
          .addClass('indexing');
      }

      // show raw status

      var code = $(
        '<pre class="syntax language-json"><code>' +
        app.format_json(xhr.responseText).esc() +
        '</code></pre>'
      );

      $('#raw_output_container', content_element).html(code);
      hljs.highlightBlock(code.get(0));

      if (!app.timeout && autorefresh_status) {
        app.timeout = window.setTimeout(
          function() {
            dataimport_fetch_status(handler_url, true)
          },
          dataimport_timeout
        );
      }

      // fill config form
      $('#cfg_name span', form_config_element).text(handler_name);
      $('#hidden_cfg_name', form_config_element).val(handler_name);
      $('#cfg_dbType', form_config_element).val(status.dihType);
      $('#cfg_schema', form_config_element).val(status.content);

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
        dataimport_timeout / 2
      );
    }
  });
}