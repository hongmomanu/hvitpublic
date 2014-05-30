/**
 * Created by jack on 14-1-6.
 */

define(function(){

    var a={
        ShowContent:function(htmlfile,jsfile,title,value,folder,res,id,businesstype){
            require(['../js/LookupItemName.js','../js/LoadingMask.js'],function(LookupItemName,LoadingMask){
                LoadingMask.ajaxLoading();
                var require_render=function(htmlfile,jsfile){

                    var options= {
                        title: title,
                        content: htmlfile,
                        id:id,
                        //businesstype:businesstype,
                        closable: true
                    };
                    if($('#tabs').tabs('exists',1)){

                        $('#tabs').tabs('select', 1);
                        $('#tabs').tabs('close',1);

                    }
                    $('#tabs').tabs('add',options);
                    LoadingMask.ajaxLoadEnd();
                    jsfile.render(res);


                };
                require([htmlfile,jsfile],require_render);


            });


        }

    }

    return a;
});
