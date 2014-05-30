define(function () {

    function render(parameters) {
        $('#newenumwin').dialog({
            title: '新增功能窗口',
            width: 300,
            height: 280,
            closed: false,
            cache: false,
            buttons:[{
                text:'保存',
                id:'savenewenumbtn',
                disabled:true,
                handler:function(){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyform,ajaxfrom){
                            var params=$('#newenumwin form').form("serialize");
                            var success=function(res){
                                if(res.success){
                                    $.messager.alert('操作成功','新增枚举成功!');
                                    $('#newenumwin').dialog('close');
                                    $('#enummanagerpanel').datagrid('reload');
                                }else{
                                    $.messager.alert('操作失败', res.msg);
                                }

                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','新增枚举失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','../auth/addnewenum',params,success,null,errorfunc)

                        });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#newenumwin').dialog('close');
                }
            }],
            modal: true
        });





        $.parser.parse($('#newenumwin'));

        $('#newenumwin input').on('change',function(){
            var form=$('#newenumwin form');
            if(form.form('validate')){
                $('#savenewenumbtn').linkbutton('enable');
            }
            else{
                $('#savenewenumbtn').linkbutton('disable');
            }
        });



    }

    return {
        render: render

    };
});