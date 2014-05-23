/**
 * Created by jack on 14-1-7.
 */
define(function(){

    var a={

        lookupitemname:function(item,value){
            var result='';
            for(var i in item){
                if(item[i]==value){
                    result=i;
                    break;
                }
            }
            return result;
        },
        lookup: function (arr, item) {

            var result = null;
            if (arr) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i][item.name] === item.value){
                        result = arr[i];
                    }
                }
            }

            return result;
        }
    };

    return a;
});
