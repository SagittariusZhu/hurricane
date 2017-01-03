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

var parse_memory_value = function( value )
{
  if( value !== Number( value ) )
  {
    var units = 'BKMGTPEZY';
    var match = value.match( /^(\d+([,\.]\d+)?) (\w)\w?$/ );
    var value = parseFloat( match[1] ) * Math.pow( 1024, units.indexOf( match[3].toUpperCase() ) );
  }
    
  return value;
};

var generate_bar = function( bar_container, bar_data, convert_label_values )
{
  bar_holder = $( '.bar-holder', bar_container );

  var bar_level = 1;
  var max_width = Math.round( $( '.bar-max', bar_holder ).width() );
  $( '.bar-max.val', bar_holder ).text( bar_data['max'] );
    
  bar_level++;
  $( '.bar-total.bar', bar_holder ).width( new String( (bar_data['total']/bar_data['max'])*100 ) + '%' );
  $( '.bar-total.val', bar_holder ).text( bar_data['total'] );

  if( bar_data['used'] )
  {
    bar_level++;
    $( '.bar-used.bar', bar_holder ).width( new String( (bar_data['used']/bar_data['total'])*100 ) + '%' );
    $( '.bar-used.val', bar_holder ).text( bar_data['used'] );
  }

  bar_holder
    .addClass( 'bar-lvl-' + bar_level );

  var percentage = ( ( ( bar_data['used'] || bar_data['total'] ) / bar_data['max'] ) * 100 ).toFixed(1);
        
  var hl = $( '[data-desc="' + bar_container.attr( 'id' ) + '"]' );

  $( '.bar-desc', hl )
    .remove();

  hl
    .append( ' <small class="bar-desc">' + percentage + '%</small>' );

  if( !!convert_label_values )
  {
    $( '.val', bar_holder )
      .each
      (
        function()
        {
          var self = $( this );

          var unit = null;
          var byte_value = parseInt( self.html() );

          self
            .attr( 'title', 'raw: ' + byte_value + ' B' );

          byte_value /= 1024;
          byte_value /= 1024;
          unit = 'MB';

          if( 1024 <= byte_value )
          {
            byte_value /= 1024;
            unit = 'GB';
          }

          byte_value = byte_value.toFixed( 2 ) + ' ' + unit;

          self
            .text( byte_value );
        }
      );
  }
};

var system_info = function( element, system_data )
{
  // -- usage

  // -- physical-memory-bar
    
  var bar_holder = $( '#physical-memory-bar', element );
  if( system_data['system']['totalMemory'] === undefined || system_data['system']['freeMemory'] === undefined )
  {
    bar_holder.hide();
  }
  else
  {
    bar_holder.show();

    var bar_data = {
      'max' : parse_memory_value( system_data['system']['totalMemory']*1000 ),
      'total' : parse_memory_value( (system_data['system']['totalMemory'] - system_data['system']['freeMemory'] )*1000)
    };

    generate_bar( bar_holder, bar_data, true );
  }

  // -- swap-space-bar


  0 === $( '#system div[id$="-bar"]:visible', element ).size()
    ? $( '#system .no-info', element ).show()
    : $( '#system .no-info', element ).hide();

  // -- memory-bar

  var bar_holder = $( '#jvm-memory-bar', element );
  if( system_data['jvminfo']=== undefined )
  {
    bar_holder.hide();
  }
  else
  {
    bar_holder.show();


    var bar_data = {
      'max' : parse_memory_value( system_data['jvminfo']['max']*1024 ),
      'total' : parse_memory_value( system_data['jvminfo']['capacity']*1024 ),
      'used' : parse_memory_value( system_data['jvminfo']['used']*1024 )
    };

    generate_bar( bar_holder, bar_data, true );
  }

}

sammy.get
(
  /^#\/$/,
  function( context )
  {
    var content_element = $( '#content' );

    content_element
      .html( '<div id="index"></div>' );

    $.ajax
    (
      {
        url : 'tpl/index.html',
        context : $( '#index', content_element ),
        beforeSend : function( arr, form, options )
        {
        },
        success : function( template )
        {
          var self = this;

          this
            .html( template );
          
  
          var data = {
            'start_time' : app.dashboard_values['data']['start'],
            'jvm_version' : app.dashboard_values['data']['jvmversion'] ,
            'hmw_spec_version' : app.dashboard_values['data']['hmw-spec'],
            'solr_version' : app.dashboard_values['data']['solr']['version'],
            'solr_cloud' : app.dashboard_values['data']['solr']['isCloud'],
            'hdfs_version' : app.dashboard_values['data']['hdfs']['version']
          };
    
          for( var key in data )
          {                                                        
            var value_element = $( '.' + key + ' dd', this );

            if (typeof data[key] == String)
              value_element
                .text( data[key].esc() );
            else
              value_element
                .text( data[key] );
                        
            value_element.closest( 'li' )
              .show();
          }

          var zooinfo = app.dashboard_values['data']['zooInfo'];
          if (0 !== zooinfo.length) {
            var zoo_key_element = $( '.zoo_info dt', this );
            var zoo_element = $( '.zoo_info dd', this );

            zoo_element = $("<dd></dd>");
            zoo_element.text(zooinfo['liveNodes'].esc());
            zoo_key_element.after( zoo_element );

            zoo_element = $("<dd></dd>");
            zoo_element.text(zooinfo['version'].esc());
            zoo_key_element.after( zoo_element );

            $( '.zoo_info dd:last', this )
              .remove();
            
            zoo_key_element.closest( 'li' )
              .show();
          }

          var ftpServer = app.dashboard_values['data']['ftpserverInfo'];
          if (0 !== ftpServer.length) {
            var ftp_key_element = $( '.ftp_info dt', this );
            var ftp_element = $( '.ftp_info dd', this );

            if (ftpServer['enable']) {
              ftp_element = $("<dd></dd>");
              var value = ftpServer['workMode'].esc();
              if (ftpServer['workPort'] > 0) {
                value += " with Port=" + ftpServer['workPort'];
              } else {
                ftp_element.addClass("warning");
              }
              ftp_element.text(value);
              ftp_key_element.after( ftp_element );

              ftp_element = $("<dd></dd>");
              ftp_element.text(ftpServer['version'].esc());
              ftp_key_element.after( ftp_element );
            } else {
              ftp_element = $("<dd></dd>");
              ftp_element.text('Disabled');
              ftp_key_element.after( ftp_element );              
            }

            $( '.ftp_info dd:last', this )
              .remove();
            
            ftp_key_element.closest( 'li' )
              .show();
          }

          var solr = app.dashboard_values['data']['solr'];
          if( 0 !== solr.length )
          {
            var solr_key_element = $( '.solr_nodes dt', this );
            var solr_element = $( '.solr_nodes dd', this );

            for( var key in solr['liveNodes'] )
            {
              solr_element = solr_element.clone();
              solr_element.text( key);

              solr_key_element
                .after( solr_element );
            }

            $( '.solr_nodes dd:last', this )
              .remove();
            
            solr_key_element.closest( 'li' )
              .show();

            // -- solr-space-bar
            var bar_holder = $( '#solr-space-bar', this );
            if( solr['max'] === undefined || solr['used'] === undefined )
            {
              bar_holder.hide();
            }
            else
            {
              bar_holder.show();

              var bar_data = {
                'total' : parse_memory_value( solr['used'] ),
                'max' : parse_memory_value( solr['max'] )
              };

              generate_bar( bar_holder, bar_data ,true);
            }   

            $( '#solr .no-info', this ).hide();              
          }

          var hdfs = app.dashboard_values['data']['hdfs'];
          if( 0 !== hdfs.length )
          {
            var hdfs_nn_element = $( '.hdfs_nnodes dt', this );
            var hdfs_element = $( '.hdfs_nnodes dd', this );

            for( var key in hdfs['nameNodes'] )
            {
              hdfs_element = $("<dd></dd>");
              hdfs_element.text( key);
              hdfs_element.addClass(hdfs['nameNodes'][key]);

              hdfs_nn_element
                .after( hdfs_element );
            }

            $( '.hdfs_nnodes dd:last', this )
              .remove();

            hdfs_nn_element.closest( 'li' )
              .show();

            var hdfs_dn_element = $( '.hdfs_dnodes dt', this );
            hdfs_element = $( '.hdfs_dnodes dd', this );

            for( var key in hdfs['liveNodes'] )
            {
              hdfs_element = hdfs_element.clone();
              hdfs_element.text( key);

              hdfs_dn_element
                .after( hdfs_element );
            }

            $( '.hdfs_dnodes dd:last', this )
              .remove();

            hdfs_dn_element.closest( 'li' )
              .show();

            // -- hdfs-space-bar
            var bar_holder = $( '#hdfs-space-bar', this );
            if( hdfs['max'] === undefined || hdfs['used'] === undefined )
            {
              bar_holder.hide();
            }
            else
            {
              bar_holder.show();

              var bar_data = {
                'total' : parse_memory_value( hdfs['used'] ),
                'max' : parse_memory_value( hdfs['max'] )
              };

              generate_bar( bar_holder, bar_data ,true);
            }   

            $( '#hdfs .no-info', this ).hide();              
          }

          $( '.timeago', this )
            .timeago();

          $( '.index-left .block li:visible:odd', this )
            .addClass( 'odd' );
                    
          // -- system_info

          system_info( this, app.dashboard_values['data'] );

          $( '#system a.reload', this )
            .die( 'click' )
            .live
            (
              'click',
              function( event )
              {
                $.ajax
                (
                  {
                    url : app_config.url+app_config.solr_path + '/admin/system?wt=json',
                    dataType : 'json',
                    context : this,
                    beforeSend : function( arr, form, options )
                    {
                      loader.show( this );
                    },
                    success : function( response )
                    {
                      system_info( self, response.data );
                    },
                    error : function()
                    {
                    },
                    complete : function()
                    {
                      loader.hide( this );
                    }
                  }
                );

                return false;
              }
            );
        },
        error : function( xhr, text_status, error_thrown )
        {
        },
        complete : function( xhr, text_status )
        {
        }
      }
    );
  }
);
