define(function () {

    function render(parameters) {
        $('#newrolewin').dialog({
            title: '新增角色窗口',
            width: 300,
            height: 280,
            closed: false,
            cache: false,
            buttons:[{
                text:'保存',
                id:'savenewrolebtn',
                disabled:true,
                handler:function(){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyform,ajaxfrom){
                            var params=$('#newrolewin form').form("serialize");
                            var success=function(res){
                                if(res.success){
                                    $.messager.alert('操作成功','新增角色成功!');
                                    $('#newrolewin').dialog('close');
                                    $('#rolemanagerpanel').datagrid('reload');
                                }else{
                                    $.messager.alert('操作失败',res.msg);
                                }

                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','新增功能失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','../auth/addnewrole',params,success,null,errorfunc)

                        });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#newrolewin').dialog('close');
                }
            }],
            modal: true
        });

        $.parser.parse($('#newrolewin'));

        $('#newrolewin input').on('change',function(){
            var form=$('#newrolewin form');
            if(form.form('validate')){
                $('#savenewrolebtn').linkbutton('enable');
            }
            else{
                $('#savenewrolebtn').linkbutton('disable');
            }
        });

    }

    return {
        render: render

    };
});