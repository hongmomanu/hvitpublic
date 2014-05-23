define(function () {

    function render(parameters) {
        $('#newfuncwin').dialog({
            title: '新增功能窗口',
            width: 300,
            height: 280,
            closed: false,
            cache: false,
            buttons:[{
                text:'保存',
                id:'savenewfuncbtn',
                disabled:true,
                handler:function(){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyform,ajaxfrom){
                            var params=$('#newfuncwin form').form("serialize");
                            var success=function(){
                                $.messager.alert('操作成功','新增功能成功!');
                                $('#newfuncwin').dialog('close');
                                $('#funcmanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','新增功能失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','ajax/addnewfunc.jsp',params,success,null,errorfunc)

                        });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#newfuncwin').dialog('close');
                }
            }],
            modal: true
        });





        $.parser.parse($('#newfuncwin'));

        $('#newfuncwin input').on('change',function(){
            var form=$('#newfuncwin form');
            if(form.form('validate')){
                $('#savenewfuncbtn').linkbutton('enable');
            }
            else{
                $('#savenewfuncbtn').linkbutton('disable');
            }
        });



    }

    return {
        render: render

    };
});