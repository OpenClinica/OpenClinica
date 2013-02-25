var ParseUtil = Backbone.Model.extend({}, {

  parseYesNo: function(value) {
    if (value == "Yes") {
      return true;
    }
    else if (value == "No") {
      return false;
    }
    return false;
  }

});
