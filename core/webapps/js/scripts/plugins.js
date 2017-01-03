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

sammy.bind(
  'plugins_load_data',
  function(event, params) {
    var core_basepath = params.active_core.attr('data-basepath');
    $.ajax({
      url: config.url + core_basepath + '/dih?wt=json&command=list',
      dataType: 'json',
      beforeSend: function(xhr, settings) {},
      success: function(response, text_status, xhr) {
        var plugin_handlers = [
          {name: "geo", desc: "China Congressional Districts"}, 
          {name: "tree", desc: "English Word Treemap"},
          {name: "bar", desc: "Hierarchical Bar Chart"}
        ];
        
        var has_handler = false;
        for( handler in plugin_handlers ) {
          has_handler = true; break;
        }
        
        if( has_handler ) {
          params.success( plugin_handlers );
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
  'plugins_build_navigation',
  function( event, params )
  {
    var list_element = $('ul', params.navigation_element);
    var navigation_content = [];

    for (var i = 0; i < params.plugins.length; i++) {
      navigation_content.push(
        '<li id="' + params.plugins[i].name + '"><a href="' + params.basepath + params.plugins[i].name + '">' + params.plugins[i].desc +
        '</a></li>'
      );
    }

    $(list_element)
      .html(navigation_content.join("\n"));

    $('a[href="' + params.basepath + params.current_plugin + '"]', list_element).closest('li')
      .addClass('current');      
  }
);

sammy.bind
(
  'plugins_load_template',
  function( event, params )
  {
    if( app.plugins_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/plugins.html',
      function( template )
      {
        params.content_element
          .html( template );
             
        app.plugins_template = template;   
        params.callback();
      }
    );
  }
);

// #/:core/plugins
sammy.get
(
  new RegExp( app.core_regex_base + '\\/(plugins)$' ),
  function( context )
  {
    delete app.plugins_template;
    var active_core = this.active_core;
    var core_basepath = active_core.attr( 'data-basepath' );
    var content_element = $( '#content' );
 
    sammy.trigger(
      'plugins_load_data', {
        active_core: this.active_core,
        success: function( plugin_handlers ) {
          context.redirect(context.path + '/' + plugin_handlers[0].name);              
        },
        error: function() {
          sammy.trigger
          (
            'plugins_load_template',
            {
              content_element : content_element,
              callback : function()
              {

              }
            });
        }
      });
  }
);

// #/:core/plugins
sammy.get
(
  new RegExp(app.core_regex_base + '\\/(plugins)\\/'),
  function( context )
  {
    var active_core = this.active_core;
    var core_basepath = active_core.attr( 'data-basepath' );
    var content_element = $( '#content' );
    var path_parts = this.path.match(/^(.+\/plugins\/)(.*)$/);
    var current_plugin = path_parts[2];
    var nav_basepath = path_parts[1];
 
    sammy.trigger(
      'plugins_load_data', {
        active_core: this.active_core,
        error: function() {
          context.redirect( '#/' + context.params.splat[0] );
        },
        success: function(plugin_handlers) {
          sammy.trigger
          (
            'plugins_load_template',
            {
              content_element : content_element,
              callback : function() {
                var navigation_element = $( '#navigation', content_element);
                var frame_element = $('#frame', content_element);

                frame_element.empty();

                sammy.trigger(
                  'plugins_build_navigation',
                  {
                    navigation_element : navigation_element,
                    plugins : plugin_handlers,
                    basepath : nav_basepath,
                    current_plugin : current_plugin
                  });
                if ('geo' == current_plugin)
                  drawGeo();
                else if ("tree" == current_plugin)
                  textAnalysis();
                else if ("bar" == current_plugin)
                  dynamicBar();
              }
            });
        }
      }
    );
  }
);

function drawGeo() {
  var width  = 800;
  var height = 600;
  
  var svg = d3.select("#frame").append("svg")
      .attr("width", width)
      .attr("height", height)
      .append("g")
      .attr("transform", "translate(0,0)");
 
  var projection = d3.geo.mercator()
            .center([107, 31])
            .scale(640)
            .translate([width/2 + 50, height/2 + 100]);
  
  var path = d3.geo.path()
          .projection(projection);
  
  
  var color = d3.scale.category20();
  
  
  d3.json("data/china.json", function(error, root) {
    
    if (error) 
      return console.error(error);
    console.log(root.features);
    
    svg.selectAll("path")
      .data( root.features )
      .enter()
      .append("path")
      .attr("stroke","#000")
      .attr("stroke-width",1)
      .attr("fill", function(d,i){
        return color(i);
      })
      .attr("d", path )
      .on("mouseover",function(d,i){
                d3.select(this)
                    .attr("fill","yellow");
            })
            .on("mouseout",function(d,i){
                d3.select(this)
                    .attr("fill",color(i));
            });
    
  });
}

function textAnalysis() {
  var margin = {top: 40, right: 10, bottom: 10, left: 10},
      width = 910 - margin.left - margin.right,
      height = 500 - margin.top - margin.bottom;

  var color = d3.scale.category20c();

  var toggleValue = false;

  var treemap = d3.layout.treemap()
      .size([width, height])
      .sticky(true)
      .value(function(d) { return d.size; });

  var div = d3.select("#frame").append("div")
      .style("position", "relative")
      .style("width", (width + margin.left + margin.right) + "px")
      .style("height", (height + margin.top + margin.bottom) + "px")
      .style("left", margin.left + "px")
      .style("top", margin.top + "px");

  d3.json("data/flare.json", function(error, root) {
    var node = div.datum(root).selectAll(".node")
        .data(treemap.nodes)
      .enter().append("div")
        .attr("class", "node")
        .call(position)
        .style("background", function(d) { return d.children ? color(d.name) : null; })
        .text(function(d) { return d.children ? null : d.name; });

    d3.selectAll(".node").on("click", function change() {
      toggleValue = !toggleValue;
      var value = toggleValue
          ? function() { return 1; }
          : function(d) { return d.size; };

      node
          .data(treemap.value(value).nodes)
        .transition()
          .duration(1500)
          .call(position);
    });
  });

  function position() {
    this.style("left", function(d) { return d.x + "px"; })
        .style("top", function(d) { return d.y + "px"; })
        .style("width", function(d) { return Math.max(0, d.dx - 1) + "px"; })
        .style("height", function(d) { return Math.max(0, d.dy - 1) + "px"; });
  }
}

function dynamicBar() {
  var margin = {top: 30, right: 120, bottom: 0, left: 120},
    width = 960 - margin.left - margin.right,
    height = 600 - margin.top - margin.bottom;

  var x = d3.scale.linear()
      .range([0, width]);

  var barHeight = 20;

  var color = d3.scale.ordinal()
      .range(["steelblue", "#ccc"]);

  var duration = 750,
      delay = 25;

  var partition = d3.layout.partition()
      .value(function(d) { return d.size; });

  var xAxis = d3.svg.axis()
      .scale(x)
      .orient("top");

  var svg = d3.select("#frame").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  svg.append("rect")
      .attr("class", "background")
      .attr("width", width)
      .attr("height", height)
      .on("click", up);

  svg.append("g")
      .attr("class", "x axis");

  svg.append("g")
      .attr("class", "y axis")
    .append("line")
      .attr("y1", "100%");

  d3.json("data/dynbar.json", function(error, root) {
    partition.nodes(root);
    x.domain([0, root.value]).nice();
    down(root, 0);
  });

  function down(d, i) {
    if (!d.children || this.__transition__) return;
    var end = duration + d.children.length * delay;

    // Mark any currently-displayed bars as exiting.
    var exit = svg.selectAll(".enter")
        .attr("class", "exit");

    // Entering nodes immediately obscure the clicked-on bar, so hide it.
    exit.selectAll("rect").filter(function(p) { return p === d; })
        .style("fill-opacity", 1e-6);

    // Enter the new bars for the clicked-on data.
    // Per above, entering bars are immediately visible.
    var enter = bar(d)
        .attr("transform", stack(i))
        .style("opacity", 1);

    // Have the text fade-in, even though the bars are visible.
    // Color the bars as parents; they will fade to children if appropriate.
    enter.select("text").style("fill-opacity", 1e-6);
    enter.select("rect").style("fill", color(true));

    // Update the x-scale domain.
    x.domain([0, d3.max(d.children, function(d) { return d.value; })]).nice();

    // Update the x-axis.
    svg.selectAll(".x.axis").transition()
        .duration(duration)
        .call(xAxis);

    // Transition entering bars to their new position.
    var enterTransition = enter.transition()
        .duration(duration)
        .delay(function(d, i) { return i * delay; })
        .attr("transform", function(d, i) { return "translate(0," + barHeight * i * 1.2 + ")"; });

    // Transition entering text.
    enterTransition.select("text")
        .style("fill-opacity", 1);

    // Transition entering rects to the new x-scale.
    enterTransition.select("rect")
        .attr("width", function(d) { return x(d.value); })
        .style("fill", function(d) { return color(!!d.children); });

    // Transition exiting bars to fade out.
    var exitTransition = exit.transition()
        .duration(duration)
        .style("opacity", 1e-6)
        .remove();

    // Transition exiting bars to the new x-scale.
    exitTransition.selectAll("rect")
        .attr("width", function(d) { return x(d.value); });

    // Rebind the current node to the background.
    svg.select(".background")
        .datum(d)
      .transition()
        .duration(end);

    d.index = i;
  }

  function up(d) {
    if (!d.parent || this.__transition__) return;
    var end = duration + d.children.length * delay;

    // Mark any currently-displayed bars as exiting.
    var exit = svg.selectAll(".enter")
        .attr("class", "exit");

    // Enter the new bars for the clicked-on data's parent.
    var enter = bar(d.parent)
        .attr("transform", function(d, i) { return "translate(0," + barHeight * i * 1.2 + ")"; })
        .style("opacity", 1e-6);

    // Color the bars as appropriate.
    // Exiting nodes will obscure the parent bar, so hide it.
    enter.select("rect")
        .style("fill", function(d) { return color(!!d.children); })
      .filter(function(p) { return p === d; })
        .style("fill-opacity", 1e-6);

    // Update the x-scale domain.
    x.domain([0, d3.max(d.parent.children, function(d) { return d.value; })]).nice();

    // Update the x-axis.
    svg.selectAll(".x.axis").transition()
        .duration(duration)
        .call(xAxis);

    // Transition entering bars to fade in over the full duration.
    var enterTransition = enter.transition()
        .duration(end)
        .style("opacity", 1);

    // Transition entering rects to the new x-scale.
    // When the entering parent rect is done, make it visible!
    enterTransition.select("rect")
        .attr("width", function(d) { return x(d.value); })
        .each("end", function(p) { if (p === d) d3.select(this).style("fill-opacity", null); });

    // Transition exiting bars to the parent's position.
    var exitTransition = exit.selectAll("g").transition()
        .duration(duration)
        .delay(function(d, i) { return i * delay; })
        .attr("transform", stack(d.index));

    // Transition exiting text to fade out.
    exitTransition.select("text")
        .style("fill-opacity", 1e-6);

    // Transition exiting rects to the new scale and fade to parent color.
    exitTransition.select("rect")
        .attr("width", function(d) { return x(d.value); })
        .style("fill", color(true));

    // Remove exiting nodes when the last child has finished transitioning.
    exit.transition()
        .duration(end)
        .remove();

    // Rebind the current parent to the background.
    svg.select(".background")
        .datum(d.parent)
      .transition()
        .duration(end);
  }

  // Creates a set of bars for the given data node, at the specified index.
  function bar(d) {
    var bar = svg.insert("g", ".y.axis")
        .attr("class", "enter")
        .attr("transform", "translate(0,5)")
      .selectAll("g")
        .data(d.children)
      .enter().append("g")
        .style("cursor", function(d) { return !d.children ? null : "pointer"; })
        .on("click", down);

    bar.append("text")
        .attr("x", -6)
        .attr("y", barHeight / 2)
        .attr("dy", ".35em")
        .style("text-anchor", "end")
        .text(function(d) { return d.name; });

    bar.append("rect")
        .attr("width", function(d) { return x(d.value); })
        .attr("height", barHeight);

    return bar;
  }

  // A stateful closure for stacking bars horizontally.
  function stack(i) {
    var x0 = 0;
    return function(d) {
      var tx = "translate(" + x0 + "," + barHeight * i * 1.2 + ")";
      x0 += x(d.value);
      return tx;
    };
  }
}