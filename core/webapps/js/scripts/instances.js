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

sammy.bind
(
  'instances_load_data',
  function( event, params )
  {
    $.ajax
    (
      {
        url : app.config.hurricane_path + app.config.instance_admin_path + '?wt=json',
        dataType : 'json',
        beforeSend : function( xhr, settings )
        {
        },
        success : function( response, text_status, xhr )
        {
          if( params.only_failures )
          {
            app.check_for_init_failures( response );
            return true;
          }

          var has_instances = true;
          for( instance in response.status )
          {
            has_instances = true; break;
          }

          app.set_instances_data( response );
          
          if( has_instances )
          {
            params.success( app.instances_data );
          }
          else
          {
            params.error();
          }
        },
        error : function( xhr, text_status, error_thrown)
        {
        },
        complete : function( xhr, text_status )
        {
        }
      }
    );
  }
);

sammy.bind
(
  'instances_build_navigation',
  function( event, params )
  {
    var navigation_content = ['<ul>'];

    for( var idx in params.instances )
    {
      var instance = params.instances[idx];
      var instance_name = instance.name;
      if( !instance_name )
      {
        instance_name = '<em>(empty)</em>';
      }
      navigation_content.push( '<li><a href="' + params.basepath + instance_name + '">' + instance.desc + '</a></li>' );
    }

    params.navigation_element
      .html( navigation_content.join( "\n" ) );
        
    $( 'a[href="' + params.basepath + params.current_instance + '"]', params.navigation_element ).parent()
      .addClass( 'current' );
  }
);

sammy.bind
(
  'instances_load_template',
  function( event, params )
  {
    if( app.instances_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/instances.html',
      function( template )
      {
        params.content_element
          .html( template );
             
        app.instances_template = template;   
        params.callback();
      }
    );
  }
);

sammy.bind
(
  'instance_load_table',
  function( event, params )
  {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        $('#cpu').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {

                        // set up the updating of the chart each second
                        var series = this.series[0];
                        setInterval(function () {
                            var x = (new Date()).getTime(), // current time
                                y = Math.random();
                            series.addPoint([x, y], true, true);
                        }, 3000);
                    }
                }
            },
            title: {
                text: 'CPU'
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'Value'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Random data',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -19; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: Math.random()
                        });
                    }
                    return data;
                }())
            }]
        });

        $('#mem').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {

                        // set up the updating of the chart each second
                        var series = this.series[0];
                        setInterval(function () {
                            var x = (new Date()).getTime(), // current time
                                y = Math.random();
                            series.addPoint([x, y], true, true);
                        }, 3000);
                    }
                }
            },
            title: {
                text: 'Memory'
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'Value'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Random data',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -19; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: Math.random()
                        });
                    }
                    return data;
                }())
            }]
        });

        $('#disk').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {

                        // set up the updating of the chart each second
                        var series = this.series[0];
                        setInterval(function () {
                            var x = (new Date()).getTime(), // current time
                                y = Math.random();
                            series.addPoint([x, y], true, true);
                        }, 3000);
                    }
                }
            },
            title: {
                text: 'Disk'
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'Value'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Random data',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -19; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: Math.random()
                        });
                    }
                    return data;
                }())
            }]
        });
  }
);

// #/~instances
sammy.get
(
  /^#\/(~instances)$/,
  function( context )
  {
    delete app.instances_template;
    var content_element = $( '#content' );

    sammy.trigger
    (
      'instances_load_data',
      {
        success : function( instances )
        {
          var first_instance = null;
          for( var key in instances )
          {
            if( !first_instance )
            {
              first_instance = instances[key].name;
            }
            continue;
          }
          context.redirect( context.path + '/' + first_instance );
        },
        error : function()
        {
          sammy.trigger
          (
            'instances_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var instances_element = $( '#instances', content_element );
                var navigation_element = $( '#navigation', instances_element );
                var data_element = $( '#data', instances_element );
                var instance_data_element = $( '#instance-data', data_element );
                var index_data_element = $( '#index-data', data_element );

                // layout

                var ui_block = $( '#ui-block' );
                var actions_element = $( '.actions', instances_element );
                var div_action = $( 'div.action', actions_element );

                ui_block
                  .css( 'opacity', 0.7 )
                  .width( instances_element.width() + 10 )
                  .height( instances_element.height() );

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

                $( 'form', div_action )
                  .ajaxForm
                  (
                    {
                      url : app.config.solr_path + app.config.instance_admin_path + '?wt=json&indexInfo=false',
                      dataType : 'json',
                      beforeSubmit : function( array, form, options )
                      {
                        $( 'button[type="submit"] span', form )
                          .addClass( 'loader' );
                      },
                      success : function( response, status_text, xhr, form )
                      {
                        delete app.instances_data;
                        sammy.refresh();

                        $( 'button.reset', form )
                          .trigger( 'click' );
                      },
                      error : function( xhr, text_status, error_thrown )
                      {
                        var response = null;
                        eval( 'response = ' + xhr.responseText + ';' );

                        var error_elem = $( '.error', div_action.filter( ':visible' ) );
                        error_elem.show();
                        $( 'span', error_elem ).text( response.error.msg );
                      },
                      complete : function()
                      {
                        $( 'button span.loader', actions_element )
                          .removeClass( 'loader' );
                      }
                    }
                  );

                // --

                $( '#add', content_element )
                  .trigger( 'click' );

                $( '[data-rel="add"] input[type="text"]:first', content_element )
                  .focus();
              }
            }
          );
        }
      }
    );
  }
);

// #/~instances
sammy.get
(
  /^#\/(~instances)\//,
  function( context )
  {
    var content_element = $( '#content' );

    var path_parts = this.path.match( /^(.+\/~instances\/)(.*)$/ );
    var current_instance = path_parts[2];

    sammy.trigger
    (
      'instances_load_data',
      {
        error : function()
        {
          context.redirect( '#/' + context.params.splat[0] );
        },
        success : function( instances )
        {
          sammy.trigger
          (
            'instances_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var instances_element = $( '#instances', content_element );
                var navigation_element = $( '#navigation', instances_element );
                var data_element = $( '#data', instances_element );
                var instance_data_element = $( '#instance-data', data_element );
                var index_data_element = $( '#index-data', data_element );

                instances_element
                  .removeClass( 'empty' );

                sammy.trigger
                (
                  'instances_build_navigation',
                  {
                    instances : instances,
                    basepath : path_parts[1],
                    current_instance : current_instance,
                    navigation_element : navigation_element
                  }
                );

                var instance_data = app.get_instance_data(current_instance, instances);
                var instance_basepath = $( '#' + current_instance, app.menu_element ).attr( 'data-basepath' );

                // instance-data

                $( '.dbType dd', instance_data_element )
                  .html( instance_data.dbType );

                $( '.connectURL dd', instance_data_element )
                  .html( instance_data.connectURL );

                $( '.mainTable dd', instance_data_element )
                  .html( instance_data.mainTable );

                // tables-data

                var levels = ["left join", "right join", "Unwatch"];

                sammy.trigger
                (
                  'instance_load_table',
                  {
                    instance_data : instance_data,
                    levels : levels,
                    index_data_element : index_data_element
                  }
                );


                var instance_names = [];
                var instance_selects = $( '#actions select', instances_element );

                for( var key in instances )
                {
                  instance_names.push( '<option value="' + key + '">' + key + '</option>' )
                }

                instance_selects
                  .html( instance_names.join( "\n") );

                $( 'option[value="' + current_instance + '"]', instance_selects.filter( '.other' ) )
                  .remove();
                
                $( 'input[data-instance="current"]', instances_element )
                  .val( current_instance );

                // layout

                var ui_block = $( '#ui-block' );
                var actions_element = $( '.actions', instances_element );
                var div_action = $( 'div.action', actions_element );

                ui_block
                  .css( 'opacity', 0.7 )
                  .width( instances_element.width() + 10 )
                  .height( instances_element.height() );

                if( $( '#cloud.global' ).is( ':visible' ) )
                {
                  $( '.cloud', div_action )
                    .show();
                }

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

                var form_callback = {

                  rename : function( form, response )
                  {
                    var url = path_parts[1] + $( 'input[name="other"]', form ).val();
                    context.redirect( url );
                  }

                };

                $( 'form', div_action )
                  .ajaxForm
                  (
                    {
                      url : app.config.solr_path + app.config.instance_admin_path + '?wt=json&indexInfo=false',
                      dataType : 'json',
                      beforeSubmit : function( array, form, options )
                      {
                        $( 'button[type="submit"] span', form )
                          .addClass( 'loader' );
                      },
                      success : function( response, status_text, xhr, form )
                      {
                        var action = $( 'input[name="action"]', form ).val().toLowerCase();

                        delete app.instances_data;

                        if( form_callback[action] )
                        {
                         form_callback[action]( form, response ); 
                        }
                        else
                        {
                          sammy.refresh();
                        }

                        $( 'button.reset', form )
                          .trigger( 'click' );
                      },
                      error : function( xhr, text_status, error_thrown )
                      {
                        var response = null;
                        eval( 'response = ' + xhr.responseText + ';' );

                        var error_elem = $( '.error', div_action.filter( ':visible' ) );
                        error_elem.show();
                        $( 'span', error_elem ).text( response.error.msg );
                      },
                      complete : function()
                      {
                        $( 'button span.loader', actions_element )
                          .removeClass( 'loader' );
                      }
                    }
                  );

                var config_button = $( '#actions #config', instances_element );
                config_button
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      var self = $( this );

                      self
                        .toggleClass( 'open' );

                      var form_element = $( 'form', actions_element );

                      $( '#config_name', form_element )
                        .val( current_instance );

                      $( '.action.' + self.attr( 'id' ), actions_element )
                        .trigger( 'open' );

                      return false;
                    }
                  );
                                
                $( '#actions #delete', instances_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      var ret = confirm( 'Do you really want to delete data-instance "' + current_instance + '"?' );
                      if( !ret )
                      {
                        return false;
                      }

                      $.ajax
                      (
                        {
                          url : app.config.solr_path + app.config.instance_admin_path + '?wt=json&action=UNLOAD&instance=' + current_instance,
                          dataType : 'json',
                          context : $( this ),
                          beforeSend : function( xhr, settings )
                          {
                            $( 'span', this )
                              .addClass( 'loader' );
                          },
                          success : function( response, text_status, xhr )
                          {
                            delete app.instances_data;
                            context.redirect( path_parts[1].substr( 0, path_parts[1].length - 1 ) );
                          },
                          error : function( xhr, text_status, error_thrown )
                          {
                          },
                          complete : function( xhr, text_status )
                          {
                            $( 'span', this )
                              .removeClass( 'loader' );
                          }
                        }
                      );
                    }
                  );

                var optimize_button = $( '#actions #optimize', instances_element );
                optimize_button
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      $.ajax
                      (
                        {
                          url : instance_basepath + '/update?optimize=true&waitFlush=true&wt=json',
                          dataType : 'json',
                          context : $( this ),
                          beforeSend : function( xhr, settings )
                          {
                            $( 'span', this )
                              .addClass( 'loader' );
                          },
                          success : function( response, text_status, xhr )
                          {
                            this
                              .addClass( 'success' );

                            window.setTimeout
                            (
                              function()
                              {
                                optimize_button
                                  .removeClass( 'success' );
                              },
                              1000
                            );
                                                        
                            $( '.optimized dd.ico-0', index_data_element )
                              .removeClass( 'ico-0' )
                              .addClass( 'ico-1' );
                          },
                          error : function( xhr, text_status, error_thrown)
                          {
                            console.warn( 'd0h, optimize broken!' );
                          },
                          complete : function( xhr, text_status )
                          {
                            $( 'span', this )
                              .removeClass( 'loader' );
                          }
                        }
                      );
                    }
                  );

                $( '.timeago', data_element )
                  .timeago();

                $( 'div.content > ul', data_element )
                  .each
                  (
                    function( i, element )
                    {
                      $( '> li:odd', element )
                        .addClass( 'odd' );
                    }
                  )
              }
            }
          );
        }
      }
    );
  }
);