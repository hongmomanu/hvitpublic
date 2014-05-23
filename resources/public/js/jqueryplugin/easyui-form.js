/**
 * Created by jack on 14-1-14.
 */
$.extend($.fn.form.methods, {
    serialize: function(jq){
        var arrayValue = $(jq[0]).serializeArray();
        var json = {};
        $.each(arrayValue, function() {
            var item = this;
            if (json[item["name"]]) {
                json[item["name"]] = json[item["name"]] + "," + item["value"];
            } else {
                json[item["name"]] = item["value"];
            }
        });
        return json;
    },
    getValue:function(jq,name){
        var jsonValue = $(jq[0]).form("serialize");
        return jsonValue[name];
    },
    setValue:function(jq,name,value){
        return jq.each(function () {
            _b(this, _29);
            var data = {};
            data[name] = value;
            $(this).form("load",data);
        });
    }
});