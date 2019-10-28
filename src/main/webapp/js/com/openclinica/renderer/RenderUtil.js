var RenderUtil = Backbone.Model.extend({}, {

  // Hash of preloaded templates for the app
  templates: {},

  // Recursively pre-load all the templates for the app.
  // This implementation should be changed in a production environment. All the template files should be
  // concatenated in a single file.
  loadTemplates: function(names, callback) {
    var that = this;
    
    var loadTemplate = function(index) {
        var name = names[index];
        debug('Loading template: ' + name, util_logDebug );
        $.get(app_contextPath + '/template/' + name + '.html', function(data) {
            that.templates[name] = data;
            index++;
            if (index < names.length) {
                loadTemplate(index);
            } else {
                callback();
            }
        });
    }

    loadTemplate(0);
  },

  // Get template by name from hash of preloaded templates
  get: function(name) {
         return this.templates[name];
       },
  
  render: function (srcTemplate, values) {
    return $.tmpl(srcTemplate, values);
  }

});
