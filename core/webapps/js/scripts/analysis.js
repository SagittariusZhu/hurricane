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

// #/:core/analysis

var fake_table = '<table cellspacing="1" class="tablesorter">' +             
                    '<thead>' +
                      '<tr><th>first name</th><th>last name</th><th>age</th><th>total</th><th>discount</th><th>date</th></tr>' +
                    '</thead>' +
                    '<tbody>' +
                      '<tr><td>peter</td><td>parker</td><td>28</td><td>$9.99</td><td>20%</td><td>jul 6, 2006 8:14 am</td></tr>' +
                      '<tr><td>john</td><td>hood</td><td>33</td><td>$19.99</td><td>25%</td><td>dec 10, 2002 5:14 am</td></tr>' + 
                      '<tr><td>clark</td><td>kent</td><td>18</td><td>$15.89</td><td>44%</td><td>jan 12, 2003 11:14 am</td></tr>' +
                      '<tr><td>bruce</td><td>almighty</td><td>45</td><td>$153.19</td><td>44%</td><td>jan 18, 2001 9:12 am</td></tr>' +
                      '<tr><td>bruce</td><td>evans</td><td>22</td><td>$13.19</td><td>11%</td><td>jan 18, 2007 9:12 am</td></tr>' +
                    '</tbody>' +
                '</table>';

var fake_docs = [{"count":2924,"value":"iso-8859-1"},{"count":1549,"value":"GB2312"},{"count":622,"value":"gb2312"},{"count":589,"value":"utf-8"},{"count":403,"value":"iso-2022-jp"}];

var data = [
    ['iso-8859-1', 2924],['gb2312', 2171], ['utf-8', 589], 
    ['iso-2022-jp', 403],['others', 1604]
  ];

sammy.get
(
  new RegExp( app.core_regex_base + '\\/(analysis)$' ),
  function( context )
  {
    var active_core = this.active_core;
    var core_basepath = active_core.attr( 'data-basepath' );
    var content_element = $( '#content' );
 
    $.get
    (
      'tpl/analysis.html',
      function( template )
      {
        content_element
          .html( template );

        var canvas_area = $( '#canvas-area ul', content_element);

        var chart = $('<li class="clearfix"><div id="chartdiv"></div><div id="output" class="verbose_output"></div></li>');
        canvas_area.append(chart);

        chart.find('#chartdiv').addClass('canvas');
        chart.find('#output').html( fake_table );
                
        $.jqplot('chartdiv', [data], { 
          seriesDefaults: {
            // Make this a pie chart.
            renderer: jQuery.jqplot.PieRenderer, 
            rendererOptions: {
              // Put data labels on the pie slices.
              // By default, labels show the percentage of the slice.
              showDataLabels: true
            }
          }, 
          title: "BMJ",
          legend: { show:true, location: 'e' }
        });        
             
        var chart = $('<li class="clearfix"><div id="chartdiv2"></div><div id="output2" class="verbose_output"></div></li>');
        canvas_area.append(chart);

        chart.find('#chartdiv2').addClass('canvas');
        chart.find('#output2').html( fake_table );

        $.jqplot('chartdiv2',  [[[1, 2],[3,5.12],[5,13.1],[7,33.6],[9,85.9],[11,219.9]]], 
        { title:'Exponential Line', 
          axes:{yaxis:{min:-10, max:240}}, 
          series:[{color:'#5FAB78'}]
        });

        var chart = $('<li class="clearfix"><div id="chartdiv3"></div><div id="output3" class="verbose_output"></div></li>');
        canvas_area.append(chart);

        chart.find('#chartdiv3').addClass('canvas');
        chart.find('#output3').html( fake_table );

        $.jqplot('chartdiv3',  [[34.53, 56.32, 25.1, 18.6]], {series:[{renderer:$.jqplot.BarRenderer}]});

        $("table").tablesorter({
                // sort on the first column and third column, order asc 
                sortList: [[0,0],[2,0]] 
            }); 

      }
    );
  }
);
