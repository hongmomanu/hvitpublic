define(function () {

    function render(parameters) {
        $('#newchapterwin').dialog({
            title: '新增功能窗口',
            width: 300,
            height: 180,
            closed: false,
            cache: false,
            buttons:[{
                text:'上传',
                id:'savenewchapterbtn',
                handler:function(){

                    require(['jqueryplugin/jquery-form'],function(AjaxFormjs){
                        var success=function(data, jqForm, options)
                        {
                            //console.log(data);
                            $('#newchapterwin').dialog('close');
                            $('#divisioninfoform').form('load',{signaturepath:data.filepath});
                            /*$('#affixfilegrid').datagrid('appendRow',{
                                attachmentname: data.filename,
                                attachmentpath:data.filepath
                            });*/
                        };
                        var options = {
                            //beforeSubmit:  showRequest,  // pre-submit callback
                            dataType:"json",
                            success: success,  // post-submit callback
                            timeout:   3000
                        };
                        $('#newchapterwin form').ajaxForm(options).submit() ;

                    });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#newchapterwin').dialog('close');
                }
            }],
            modal: true
        });





        //$.parser.parse($('#newenumwin'));





    }

    return {
        render: render

    };
});